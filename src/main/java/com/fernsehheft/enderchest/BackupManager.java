package com.fernsehheft.enderchest;

import com.fernsehheft.enderchest.EnderChest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.CallSite;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class BackupManager {
    private static final String BACKUP_EXTENSION = ".ecbackup";
    private static final String BACKUP_MAGIC_V1 = "ECBK1";
    private static final String BACKUP_MAGIC_V2 = "ECBK2";
    private static final String BACKUP_MAGIC_V3 = "ECBK3";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final EnderChest plugin;
    private final File backupFolder;
    private PaperTask autoBackupTask;
    private LocalDate lastAutoBackupDate;
    private static final String RESTORE_TMP_SUFFIX = ".restoretmp";
    private static final int WEBHOOK_COLOR_SUCCESS = 3066993;
    private static final int WEBHOOK_COLOR_ERROR = 15158332;

    public BackupManager(EnderChest plugin) {
        this.plugin = plugin;
        this.backupFolder = new File(plugin.getDataFolder(), "backups");
        if (!this.backupFolder.exists()) {
            this.backupFolder.mkdirs();
        }
    }

    public void reload() {
        if (this.autoBackupTask != null) {
            this.autoBackupTask.cancel();
            this.autoBackupTask = null;
        }
        if (!this.plugin.getConfig().getBoolean("backups.auto-enabled", false)) {
            return;
        }
        this.autoBackupTask = this.plugin.getFoliaLib().getScheduler().runTimer(() -> {
            if (this.plugin.isOperationInProgress()) {
                return;
            }
            LocalTime targetTime = this.parseTime(this.plugin.getConfig().getString("backups.auto-time", "04:00"));
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            if (now.getHour() == targetTime.getHour() && now.getMinute() == targetTime.getMinute() && !today.equals(this.lastAutoBackupDate)) {
                this.lastAutoBackupDate = today;
                String autoName = "auto-" + String.valueOf(today) + "-" + String.format("%02d-%02d", now.getHour(), now.getMinute());
                this.createBackupAsync(autoName, (CommandSender)Bukkit.getConsoleSender());
            }
        }, 200L, 1200L);
    }

    public boolean isAutoBackupsEnabled() {
        return this.plugin.getConfig().getBoolean("backups.auto-enabled", false);
    }

    public String getAutoBackupTime() {
        return this.plugin.getConfig().getString("backups.auto-time", "04:00");
    }

    public void setAutoBackupSettings(boolean enabled, String time) {
        this.plugin.getConfig().set("backups.auto-enabled", (Object)enabled);
        if (time != null && !time.isBlank()) {
            this.plugin.getConfig().set("backups.auto-time", (Object)this.parseTime(time).format(TIME_FORMATTER));
        }
        this.plugin.saveConfig();
        this.reload();
    }

    public void createBackupAsync(String requestedName, CommandSender sender) {
        this.plugin.getFoliaLib().getScheduler().runNextTick(task -> {
            if (this.plugin.isOperationInProgress()) {
                sender.sendMessage(this.plugin.prefixed("&cA migration, backup, or restore is already running."));
                return;
            }
            this.plugin.getLogger().info("[Backup] Starting backup creation request: " + requestedName);
            sender.sendMessage(this.plugin.prefixed("&eBackup creation started. EnderChests are temporarily locked."));
            this.plugin.beginGlobalChestOperation(false, "&eEnderChest backup in progress. Access is briefly locked.");
            this.plugin.getFoliaLib().getScheduler().runAsync(asyncTask -> {
                try {
                    BackupInfo info = this.createBackupInternal(requestedName);
                    this.pruneOldBackups();
                    this.plugin.getFoliaLib().getScheduler().runNextTick(syncTask -> {
                        this.plugin.endGlobalChestOperation();
                        this.plugin.getLogger().info("[Backup] Backup finished successfully: " + info.getName() + " (" + info.getChestCount() + " chests)");
                        sender.sendMessage(this.plugin.prefixed("&aBackup '&f" + info.getName() + "&a' created with &f" + info.getChestCount() + "&a stored chests."));
                    });
                    this.sendWebhookSuccess("\u2705 EnderChest Backup Created", "Backup **" + info.getName() + "** created with **" + info.getChestCount() + "** stored chests.");
                }
                catch (Exception e) {
                    this.plugin.getLogger().severe("[Backup] Backup creation failed: " + e.getMessage());
                    e.printStackTrace();
                    this.plugin.getFoliaLib().getScheduler().runNextTick(syncTask -> {
                        this.plugin.endGlobalChestOperation();
                        sender.sendMessage(this.plugin.prefixed("&cCould not create the backup. Check console for details."));
                    });
                    this.sendWebhookError("\u274c EnderChest Backup Failed", "Backup **" + this.sanitizeName(requestedName) + "** could not be created.\n**Error:** " + this.describeError(e));
                }
            });
        });
    }

    public void restoreBackupAsync(String backupName, RestoreMode mode, CommandSender sender) {
        File file = this.getBackupFile(backupName);
        if (!file.exists()) {
            sender.sendMessage(this.plugin.prefixed("&cBackup '&f" + backupName + "&c' was not found."));
            return;
        }
        this.plugin.getFoliaLib().getScheduler().runNextTick(task -> {
            if (this.plugin.isOperationInProgress()) {
                sender.sendMessage(this.plugin.prefixed("&cA migration, backup, or restore is already running."));
                return;
            }
            boolean kickPlayers = mode == RestoreMode.KICK_PLAYERS;
            this.plugin.getLogger().info("[Backup] Starting restore of '" + backupName + "' using mode " + String.valueOf((Object)mode));
            sender.sendMessage(this.plugin.prefixed("&eBackup restore started. Please wait..."));
            this.plugin.beginGlobalChestOperation(kickPlayers, "&cAn EnderChest restore is running. Please wait a moment.");
            this.plugin.getFoliaLib().getScheduler().runAsync(asyncTask -> {
                try {
                    if (this.plugin.getConfig().getBoolean("backups.safety-backup-before-restore", true)) {
                        try {
                            String safetyName = "pre-restore-" + System.currentTimeMillis();
                            BackupInfo safety = this.createBackupInternal(safetyName);
                            this.plugin.getLogger().info("[Backup] Safety backup created before restore: " + safety.getName() + " (" + safety.getChestCount() + " chests)");
                            this.plugin.getFoliaLib().getScheduler().runNextTick(syncTask -> sender.sendMessage(this.plugin.prefixed("&7Safety backup '&f" + safety.getName() + "&7' created before restore.")));
                        }
                        catch (Exception safetyEx) {
                            this.plugin.getLogger().severe("[Backup] Safety backup before restore failed - aborting restore: " + safetyEx.getMessage());
                            throw new IOException("Safety backup failed, restore aborted: " + safetyEx.getMessage(), safetyEx);
                        }
                    }
                    BackupBundle bundle = this.readBackup(file);
                    this.restoreBundle(bundle);
                    this.plugin.getFoliaLib().getScheduler().runNextTick(syncTask -> {
                        this.plugin.endGlobalChestOperation();
                        this.plugin.getLogger().info("[Backup] Restore finished successfully: " + bundle.name + " (" + bundle.chests.size() + " chests)");
                        sender.sendMessage(this.plugin.prefixed("&aBackup '&f" + bundle.name + "&a' restored successfully for &f" + bundle.chests.size() + "&a chests."));
                    });
                    this.sendWebhookSuccess("\u267b\ufe0f EnderChest Backup Restored", "Backup **" + bundle.name + "** restored for **" + bundle.chests.size() + "** chests.");
                }
                catch (Exception e) {
                    this.plugin.getLogger().severe("[Backup] Backup restore failed: " + e.getMessage());
                    e.printStackTrace();
                    this.plugin.getFoliaLib().getScheduler().runNextTick(syncTask -> {
                        this.plugin.endGlobalChestOperation();
                        sender.sendMessage(this.plugin.prefixed("&cBackup restore failed. Check the console for details."));
                    });
                    this.sendWebhookError("\u274c EnderChest Restore Failed", "Restore of backup **" + backupName + "** failed.\n**Error:** " + this.describeError(e));
                }
            });
        });
    }

    public boolean deleteBackup(String backupName) {
        File file = this.getBackupFile(backupName);
        boolean result = file.exists() && file.delete();
        this.plugin.getLogger().info("[Backup] Delete request for '" + backupName + "': " + (result ? "deleted" : "not found"));
        return result;
    }

    public List<BackupInfo> listBackups() {
        ArrayList<BackupInfo> backups = new ArrayList<BackupInfo>();
        File[] files = this.backupFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(BACKUP_EXTENSION));
        if (files == null) {
            return backups;
        }
        for (File file : files) {
            try {
                BackupBundle bundle = this.readBackup(file);
                backups.add(new BackupInfo(bundle.name, bundle.createdAt, bundle.chests.size()));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        backups.sort(Comparator.comparingLong(BackupInfo::getCreatedAt).reversed());
        return backups;
    }

    private BackupInfo createBackupInternal(String requestedName) throws Exception {
        this.plugin.getLogger().info("[Backup] Collecting current EnderChest data...");
        List<StoredChest> chests = this.loadCurrentChests();
        this.plugin.getLogger().info("[Backup] Data collection finished. Entries found: " + chests.size());
        Object safeName = this.sanitizeName(requestedName);
        File file = this.getBackupFile((String)safeName);
        if (file.exists()) {
            safeName = (String)safeName + "-" + System.currentTimeMillis();
            file = this.getBackupFile((String)safeName);
        }
        long createdAt = System.currentTimeMillis();
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(file))));){
            out.writeUTF(BACKUP_MAGIC_V3);
            out.writeUTF((String)safeName);
            out.writeLong(createdAt);
            out.writeInt(chests.size());
            int processed = 0;
            int step = Math.max(1, chests.size() / 10);
            for (StoredChest chest : chests) {
                out.writeUTF(chest.uuid);
                out.writeUTF(chest.name == null ? "" : chest.name);
                byte[] dataBytes = (chest.data == null ? "" : chest.data).getBytes(StandardCharsets.UTF_8);
                out.writeInt(dataBytes.length);
                out.write(dataBytes);
                out.writeInt(chest.page);
                if (++processed % step != 0 && processed != chests.size()) continue;
                this.plugin.getLogger().info("[Backup] Writing progress: " + processed + "/" + chests.size());
            }
        }
        return new BackupInfo((String)safeName, createdAt, chests.size());
    }

    private BackupBundle readBackup(File file) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));){
            String magic = in.readUTF();
            boolean isV3 = BACKUP_MAGIC_V3.equals(magic);
            boolean isV2 = BACKUP_MAGIC_V2.equals(magic);
            boolean isV1 = BACKUP_MAGIC_V1.equals(magic);
            if (!(isV3 || isV2 || isV1)) {
                throw new IOException("Invalid backup format: " + magic);
            }
            String name = in.readUTF();
            long createdAt = in.readLong();
            int amount = in.readInt();
            ArrayList<StoredChest> chests = new ArrayList<StoredChest>(Math.max(amount, 0));
            for (int i = 0; i < amount; ++i) {
                String data;
                String uuid = in.readUTF();
                String chestName = in.readUTF();
                if (isV3) {
                    int dataLen = in.readInt();
                    byte[] dataBytes = new byte[dataLen];
                    in.readFully(dataBytes);
                    data = new String(dataBytes, StandardCharsets.UTF_8);
                } else {
                    data = in.readUTF();
                }
                int page = 0;
                if (isV2 || isV3) {
                    page = in.readInt();
                }
                chests.add(new StoredChest(uuid, chestName, data, page));
            }
            BackupBundle backupBundle = new BackupBundle(name, createdAt, chests);
            return backupBundle;
        }
    }

    private List<StoredChest> loadCurrentChests() throws Exception {
        File oldNamesFile;
        ArrayList<StoredChest> result = new ArrayList<StoredChest>();
        if (this.plugin.getStorageTypeName().equalsIgnoreCase("mysql")) {
            try (Connection conn = this.plugin.getSQLConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT uuid, name, data FROM " + this.plugin.getTableName());){
                int loaded = 0;
                while (rs.next()) {
                    String uuid = rs.getString("uuid");
                    String name2 = rs.getString("name");
                    String data = rs.getString("data");
                    int page = 0;
                    if (uuid.contains("_page")) {
                        try {
                            String pageStr = uuid.substring(uuid.lastIndexOf("_page") + 5);
                            page = Integer.parseInt(pageStr);
                            uuid = uuid.substring(0, uuid.lastIndexOf("_page"));
                        }
                        catch (Exception pageStr) {
                            // empty catch block
                        }
                    }
                    result.add(new StoredChest(uuid, name2, data, page));
                    if (++loaded % 250 != 0) continue;
                    this.plugin.getLogger().info("[Backup] Loaded " + loaded + " chest entries from MySQL...");
                }
            }
            return result;
        }
        File namesFile = new File(this.plugin.getChestDataFolder(), "player_names.yml");
        if (!namesFile.exists() && (oldNamesFile = new File(this.plugin.getDataFolder(), "player_names.yml")).exists()) {
            namesFile = oldNamesFile;
        }
        YamlConfiguration namesConfig = YamlConfiguration.loadConfiguration((File)namesFile);
        File[] chestFiles = this.plugin.getChestDataFolder().listFiles((dir, name) -> name.endsWith(".data"));
        if (chestFiles == null) {
            return result;
        }
        int loaded = 0;
        for (File chestFile : chestFiles) {
            String fileName = chestFile.getName().replace(".data", "");
            String data = "";
            try (BufferedReader reader = new BufferedReader(new FileReader(chestFile));){
                String line = reader.readLine();
                if (line != null) {
                    data = line;
                }
            }
            int page = 0;
            String uuid = fileName;
            if (fileName.contains("_page")) {
                try {
                    String pageStr = fileName.substring(fileName.lastIndexOf("_page") + 5);
                    page = Integer.parseInt(pageStr);
                    uuid = fileName.substring(0, fileName.lastIndexOf("_page"));
                }
                catch (Exception pageStr) {
                    // empty catch block
                }
            }
            String name3 = namesConfig.getString(uuid, "Unknown");
            result.add(new StoredChest(uuid, name3, data, page));
            if (++loaded % 250 != 0) continue;
            this.plugin.getLogger().info("[Backup] Loaded " + loaded + " chest files from disk...");
        }
        return result;
    }

    private void restoreBundle(BackupBundle bundle) throws Exception {
        if (this.plugin.getStorageTypeName().equalsIgnoreCase("mysql")) {
            this.restoreToMySql(bundle);
        } else {
            this.restoreToFiles(bundle);
        }
    }

    private void restoreToFiles(BackupBundle bundle) throws Exception {
        File chestDataFolder = this.plugin.getChestDataFolder();
        if (!chestDataFolder.exists()) {
            chestDataFolder.mkdirs();
        }
        this.cleanupRestoreTempFiles(chestDataFolder);
        HashSet<CallSite> validFiles = new HashSet<CallSite>();
        YamlConfiguration namesConfig = new YamlConfiguration();
        ArrayList<String[]> pendingMoves = new ArrayList<String[]>();
        this.plugin.getLogger().info("[Backup] Restoring " + bundle.chests.size() + " chests to file storage...");
        try {
            int processed = 0;
            int step = Math.max(1, bundle.chests.size() / 10);
            for (StoredChest storedChest : bundle.chests) {
                String fileName = storedChest.page > 1 ? storedChest.uuid + "_page" + storedChest.page + ".data" : storedChest.uuid + ".data";
                validFiles.add((CallSite)((Object)fileName));
                File tmp = new File(chestDataFolder, fileName + RESTORE_TMP_SUFFIX);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));){
                    writer.write(storedChest.data == null ? "" : storedChest.data);
                }
                pendingMoves.add(new String[]{tmp.getName(), fileName});
                namesConfig.set(storedChest.uuid, (Object)(storedChest.name == null || storedChest.name.isBlank() ? "Unknown" : storedChest.name));
                if (++processed % step != 0 && processed != bundle.chests.size()) continue;
                this.plugin.getLogger().info("[Backup] Restore write progress: " + processed + "/" + bundle.chests.size());
            }
            File namesTmp = new File(chestDataFolder, "player_names.yml.restoretmp");
            namesConfig.save(namesTmp);
            for (String[] pair : pendingMoves) {
                Path src = new File(chestDataFolder, pair[0]).toPath();
                Path dst = new File(chestDataFolder, pair[1]).toPath();
                Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(namesTmp.toPath(), new File(chestDataFolder, "player_names.yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
            File[] fileArray = chestDataFolder.listFiles((dir, name) -> name.endsWith(".data"));
            if (fileArray != null) {
                for (File file : fileArray) {
                    if (validFiles.contains(file.getName())) continue;
                    file.delete();
                }
            }
        }
        catch (Exception e) {
            this.cleanupRestoreTempFiles(chestDataFolder);
            throw e;
        }
    }

    private void cleanupRestoreTempFiles(File folder) {
        File[] leftovers = folder.listFiles((dir, name) -> name.endsWith(RESTORE_TMP_SUFFIX));
        if (leftovers != null) {
            for (File f : leftovers) {
                f.delete();
            }
        }
    }

    private void restoreToMySql(BackupBundle bundle) throws Exception {
        this.plugin.getLogger().info("[Backup] Restoring " + bundle.chests.size() + " chests to MySQL storage...");
        try (Connection conn = this.plugin.getSQLConnection();){
            conn.setAutoCommit(false);
            try {
                try (Statement stmt = conn.createStatement();){
                    stmt.executeUpdate("DELETE FROM " + this.plugin.getTableName());
                }
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO " + this.plugin.getTableName() + " (uuid, data, locked_by, last_lock_time, name) VALUES (?, ?, NULL, 0, ?)");){
                    int processed = 0;
                    int step = Math.max(1, bundle.chests.size() / 10);
                    for (StoredChest chest : bundle.chests) {
                        String uuid = chest.page > 1 ? chest.uuid + "_page" + chest.page : chest.uuid;
                        ps.setString(1, uuid);
                        ps.setString(2, chest.data == null ? "" : chest.data);
                        ps.setString(3, chest.name == null || chest.name.isBlank() ? "Unknown" : chest.name);
                        ps.addBatch();
                        if (++processed % step != 0 && processed != bundle.chests.size()) continue;
                        this.plugin.getLogger().info("[Backup] Restore progress: " + processed + "/" + bundle.chests.size());
                    }
                    ps.executeBatch();
                }
                conn.commit();
            }
            catch (Exception e) {
                try {
                    conn.rollback();
                    this.plugin.getLogger().warning("[Backup] MySQL restore failed - transaction rolled back, old data preserved.");
                }
                catch (Exception rollbackEx) {
                    this.plugin.getLogger().severe("[Backup] MySQL restore rollback ALSO failed: " + rollbackEx.getMessage());
                }
                throw e;
            }
        }
    }

    private void pruneOldBackups() {
        int keep = Math.max(1, this.plugin.getConfig().getInt("backups.max-backups", 15));
        List<BackupInfo> backups = this.listBackups();
        for (int i = keep; i < backups.size(); ++i) {
            this.deleteBackup(backups.get(i).getName());
        }
    }

    private File getBackupFile(String backupName) {
        return new File(this.backupFolder, this.sanitizeName(backupName) + BACKUP_EXTENSION);
    }

    private String sanitizeName(String input) {
        if (input == null || input.isBlank()) {
            return "backup-" + System.currentTimeMillis();
        }
        String normalized = input.trim().replace(' ', '-').replaceAll("[^a-zA-Z0-9._-]", "");
        if (normalized.isBlank()) {
            return "backup-" + System.currentTimeMillis();
        }
        return normalized;
    }

    private LocalTime parseTime(String input) {
        try {
            return LocalTime.parse(input, TIME_FORMATTER);
        }
        catch (DateTimeParseException ignored) {
            return LocalTime.of(4, 0);
        }
    }

    private String describeError(Throwable e) {
        if (e == null) {
            return "Unknown error";
        }
        String msg = e.getMessage();
        String type = e.getClass().getSimpleName();
        return msg == null || msg.isBlank() ? type : type + ": " + msg;
    }

    private void sendWebhookSuccess(String title, String description) {
        if (!this.plugin.getConfig().getBoolean("backups.webhook.notify-on-success", false)) {
            return;
        }
        this.sendWebhook(title, description, 3066993);
    }

    private void sendWebhookError(String title, String description) {
        this.sendWebhook(title, description, 15158332);
    }

    private void sendWebhook(String title, String description, int color) {
        if (!this.plugin.getConfig().getBoolean("backups.webhook.enabled", false)) {
            return;
        }
        String url = this.plugin.getConfig().getString("backups.webhook.url", "");
        if (url == null || url.isBlank()) {
            return;
        }
        String finalUrl = url.trim();
        String finalTitle = Messages.plain(title);
        String finalDescription = Messages.plain(description);
        this.plugin.getFoliaLib().getScheduler().runAsync(task -> {
            try {
                this.postDiscordWebhook(finalUrl, finalTitle, finalDescription, color);
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("[Backup] Could not send webhook notification: " + e.getMessage());
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void postDiscordWebhook(String webhookUrl, String title, String description, int color) throws IOException {
        String json = "{\"embeds\":[{\"title\":\"" + this.escapeJson(title) + "\",\"description\":\"" + this.escapeJson(description) + "\",\"color\":" + color + "}]}";
        HttpURLConnection conn = (HttpURLConnection)URI.create(webhookUrl).toURL().openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "EnderChest-Plugin");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream();){
                os.write(body);
            }
            int code = conn.getResponseCode();
            if (code >= 300) {
                this.plugin.getLogger().warning("[Backup] Webhook returned HTTP " + code);
            }
        }
        finally {
            conn.disconnect();
        }
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        block7: for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '\"': {
                    sb.append("\\\"");
                    continue block7;
                }
                case '\\': {
                    sb.append("\\\\");
                    continue block7;
                }
                case '\n': {
                    sb.append("\\n");
                    continue block7;
                }
                case '\r': {
                    sb.append("\\r");
                    continue block7;
                }
                case '\t': {
                    sb.append("\\t");
                    continue block7;
                }
                default: {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", c));
                        continue block7;
                    }
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public void migrateOldBackups(CommandSender sender) {
        File[] files = this.backupFolder.listFiles((dir, name) -> name.endsWith(BACKUP_EXTENSION));
        if (files == null || files.length == 0) {
            sender.sendMessage("\u00a77No backups found to migrate.");
            return;
        }
        int migrated = 0;
        int alreadyV2 = 0;
        int failed = 0;
        sender.sendMessage("\u00a7a[Migrate] Starting backup migration...");
        for (File file : files) {
            try {
                BackupBundle bundle = this.readBackup(file);
                try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));){
                    String magic = in.readUTF();
                    if (BACKUP_MAGIC_V2.equals(magic)) {
                        ++alreadyV2;
                        continue;
                    }
                }
                File tempFile = new File(this.backupFolder, file.getName() + ".tmp");
                long createdAt = bundle.createdAt;
                try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(tempFile))));){
                    out.writeUTF(BACKUP_MAGIC_V3);
                    out.writeUTF(bundle.name);
                    out.writeLong(createdAt);
                    out.writeInt(bundle.chests.size());
                    for (StoredChest chest : bundle.chests) {
                        out.writeUTF(chest.uuid);
                        out.writeUTF(chest.name == null ? "" : chest.name);
                        byte[] dataBytes = (chest.data == null ? "" : chest.data).getBytes(StandardCharsets.UTF_8);
                        out.writeInt(dataBytes.length);
                        out.write(dataBytes);
                        out.writeInt(chest.page);
                    }
                }
                if (file.delete() && tempFile.renameTo(file)) {
                    ++migrated;
                    continue;
                }
                ++failed;
                tempFile.delete();
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("[Migrate] Failed to migrate backup " + file.getName() + ": " + e.getMessage());
                ++failed;
            }
        }
        sender.sendMessage("\u00a7a[Migrate] Migration complete:");
        sender.sendMessage("  \u00a77Migrated: \u00a7a" + migrated);
        sender.sendMessage("  \u00a77Already v2: \u00a7e" + alreadyV2);
        if (failed > 0) {
            sender.sendMessage("  \u00a77Failed: \u00a7c" + failed);
        }
    }

    public static enum RestoreMode {
        KICK_PLAYERS,
        LIVE_FREEZE;

    }

    private static final class BackupBundle {
        private final String name;
        private final long createdAt;
        private final List<StoredChest> chests;

        private BackupBundle(String name, long createdAt, List<StoredChest> chests) {
            this.name = name;
            this.createdAt = createdAt;
            this.chests = chests;
        }
    }

    public static final class BackupInfo {
        private final String name;
        private final long createdAt;
        private final int chestCount;

        public BackupInfo(String name, long createdAt, int chestCount) {
            this.name = name;
            this.createdAt = createdAt;
            this.chestCount = chestCount;
        }

        public String getName() {
            return this.name;
        }

        public long getCreatedAt() {
            return this.createdAt;
        }

        public int getChestCount() {
            return this.chestCount;
        }
    }

    private static final class StoredChest {
        private final String uuid;
        private final String name;
        private final String data;
        private final int page;

        private StoredChest(String uuid, String name, String data) {
            this(uuid, name, data, 0);
        }

        private StoredChest(String uuid, String name, String data, int page) {
            this.uuid = uuid;
            this.name = name;
            this.data = data;
            this.page = page;
        }
    }
}
