package com.fernsehheft.enderchest;

import com.fernsehheft.enderchest.BackupManager;
import com.fernsehheft.enderchest.EnderChestHolder;
import com.fernsehheft.enderchest.RepairManager;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

public class EnderChest
extends JavaPlugin
implements Listener,
CommandExecutor,
TabCompleter {
    private PaperScheduler foliaLib;
    private static final Pattern UUID_IN_FILENAME_PATTERN = Pattern.compile("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    private String openSound;
    private String closeSound;
    private boolean showOpenMessage;
    private boolean showCloseMessage;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    private File namesFile;
    private FileConfiguration namesConfig;
    private File invitesFile;
    private FileConfiguration invitesConfig;
    private final Map<UUID, Inventory> activeInventories = new ConcurrentHashMap<UUID, Inventory>();
    private final Map<UUID, UUID> viewerToOwnerMap = new ConcurrentHashMap<UUID, UUID>();
    private final Map<UUID, PaperTask> lockTasks = new ConcurrentHashMap<UUID, PaperTask>();
    private final Set<String> knownPlayerNames = Collections.synchronizedSet(new HashSet<>());
    private final Set<UUID> pendingLoads = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingSaves = ConcurrentHashMap.newKeySet();
    private final Set<UUID> loadFailures = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingViewerOpens = ConcurrentHashMap.newKeySet();
    private final Set<UUID> switchingPages = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastOpenAttemptMs = new ConcurrentHashMap<UUID, Long>();
    private final Map<UUID, Long> chestGeneration = new ConcurrentHashMap<UUID, Long>();
    private String storageType;
    private File dataFolder;
    private String host;
    private String database;
    private String username;
    private String password;
    private String table;
    private int port;
    private boolean useSSL;
    private String serverId;
    private boolean rankSizeEnabled;
    private Map<String, Integer> rankSizes = new HashMap<String, Integer>();
    private int defaultSizeConfig;
    private boolean teamsEnabled;
    private int teamSizeConfig;
    private boolean isMigrating = false;
    private static final long LOCK_TIMEOUT_MS = 10000L;
    private boolean requireUsePermission;
    private boolean requireUsePermissionForCommand;
    private boolean enableBlockPermission;
    private long openCooldownMs;
    private List<Material> blacklistedItems = new ArrayList<Material>();
    private String blacklistMessage;
    private boolean pagesEnabled;
    private int maxPages;
    private final Map<UUID, Integer> playerCurrentPage = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Integer> activeInventoryPage = new ConcurrentHashMap<UUID, Integer>();
    private boolean adminAssignedRowsEnabled;
    private int adminAssignedDefaultRows;
    private int adminAssignedMaxRows;
    private File slotUpgradesFile;
    private FileConfiguration slotUpgradesConfig;
    private final Map<UUID, Integer> playerAssignedRows = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Integer> cachedRankRows = new ConcurrentHashMap<UUID, Integer>();
    private boolean adminAssignedPagesEnabled;
    private int adminAssignedDefaultPages;
    private int adminAssignedMaxPages;
    private final Map<UUID, Integer> playerAssignedPages = new ConcurrentHashMap<UUID, Integer>();
    private final Map<UUID, Integer> cachedPermissionPages = new ConcurrentHashMap<UUID, Integer>();
    private boolean autosaveEnabled;
    private int autosaveIntervalSeconds;
    private PaperTask autosaveTask;
    private final Map<UUID, Object> saveLocks = new ConcurrentHashMap<UUID, Object>();
    private BackupManager backupManager;
    private RepairManager repairManager;
    private InviteService inviteService;
    private EcSeeCommand ecSeeCommand;
    private EcShareCommand ecShareCommand;
    private TeamEnderChestCommand teamEnderChestCommand;
    private final Set<UUID> pendingBackupNameInput = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> selectedBackupRestore = new ConcurrentHashMap<UUID, String>();
    private static final String BACKUP_MAIN_TITLE = String.valueOf(ChatColor.DARK_PURPLE) + "EC Backup Center";
    private static final String BACKUP_RESTORE_LIST_TITLE = String.valueOf(ChatColor.DARK_PURPLE) + "Restore Backups";
    private static final String BACKUP_DELETE_LIST_TITLE = String.valueOf(ChatColor.DARK_PURPLE) + "Delete Backups";
    private static final String BACKUP_MODE_TITLE = String.valueOf(ChatColor.DARK_PURPLE) + "Restore Backup";
    private static final String INVITE_SCOPE_SEPARATOR = "|";
    private static final String INVITE_SCOPE_ALL = "all";
    private static final String INVITE_SCOPE_PAGE_PREFIX = "page:";
    private static final String[] ENDER_ITEMS_KEYS = new String[]{"EnderItems", "ender_items"};
    private static final String[] SLOT_KEYS = new String[]{"Slot", "slot"};

    private void flushAndCloseAllInventories() {
        this.closeAllInventories();
        long start = System.currentTimeMillis();
        long timeout = 10000L;
        while (!this.pendingSaves.isEmpty() && System.currentTimeMillis() - start < timeout) {
            try {
                Thread.sleep(50L);
            }
            catch (InterruptedException interruptedException) {}
        }
        if (!this.pendingSaves.isEmpty()) {
            this.getLogger().warning("[EnderChest] Not all Ender Chests could be saved in time! (pendingSaves: " + this.pendingSaves.size() + ")");
        }
    }

    private Object getSaveLock(UUID uuid) {
        return this.saveLocks.computeIfAbsent(uuid, k -> new Object());
    }

    private String normalizeServerId(String value) {
        return value == null ? "" : value.trim();
    }

    public void onEnable() {
        this.foliaLib = new PaperScheduler(this);
        this.sendConsole("\u00a75________________________________________________________");
        this.sendConsole("");
        this.sendConsole("  \u00a7d\u00a7lEnderChest Plugin \u00a78- \u00a77System Boot");
        this.sendConsole("  \u00a77Version: \u00a7f" + this.getDescription().getVersion());
        this.sendConsole("  \u00a77Platform: \u00a7f" + this.getServer().getName() + (this.foliaLib.isFolia() ? " (Folia)" : ""));
        this.sendConsole("  \u00a77Server Version: \u00a7f" + this.getServer().getVersion());
        this.sendConsole("\u00a75________________________________________________________");
        this.sendConsole("");
        this.saveDefaultConfig();
        this.createMessagesConfig();
        this.dataFolder = new File(this.getDataFolder(), "data");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
        this.createNamesConfig();
        this.createInvitesConfig();
        this.createSlotUpgradesConfig();
        this.loadConfigValues();
        this.backupManager = new BackupManager(this);
        this.repairManager = new RepairManager(this);
        this.inviteService = new InviteService(this);
        this.ecSeeCommand = new EcSeeCommand(this);
        this.ecShareCommand = new EcShareCommand(this);
        this.teamEnderChestCommand = new TeamEnderChestCommand(this);
        this.backupManager.reload();
        if (this.storageType.equalsIgnoreCase("mysql")) {
            this.sendConsole("\u00a7e >> \u00a7fConnecting to Database...");
            try {
                this.createTableSync();
                this.clearStaleLocks();
                this.loadNamesFromMySQL();
                this.sendConsole("\u00a7a [\u2714] \u00a7fDatabase connected, Locks initialized & Names loaded.");
            }
            catch (SQLException e) {
                this.sendConsole("\u00a7c [X] \u00a74CRITICAL: Database connection failed!");
                this.sendConsole("\u00a7c     Error: " + e.getMessage());
            }
        } else {
            this.loadNamesFromFile();
            this.sendConsole("\u00a7a [\u2714] \u00a7fUsing Local File Storage.");
        }
        this.registerCmd("ec");
        this.registerCmd("ecsee");
        this.registerCmd("ecshare");
        this.registerCmd("teamec");
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.getServer().getPluginManager().registerEvents(new EnderChestInventoryListener(this), this);
        this.warnAboutQuickEC();
        this.foliaLib.getScheduler().runLater(this::checkAutoMigration, 40L);
        this.foliaLib.getScheduler().runAsync(task -> {
            this.migratePage1Files();
            this.seedPermissionPageCacheFromStorage();
        });
    }

    private void registerCmd(String name) {
        if (this.getCommand(name) != null) {
            this.getCommand(name).setExecutor((CommandExecutor)this);
            this.getCommand(name).setTabCompleter((TabCompleter)this);
        }
    }

    public void onDisable() {
        this.sendConsole("\u00a7c________________________________________________________");
        this.sendConsole("  \u00a7c\u00a7lEnderChest Plugin \u00a78- \u00a77Shutting Down");
        if (this.autosaveTask != null) {
            this.autosaveTask.cancel();
            this.autosaveTask = null;
        }
        this.flushAndCloseAllInventories();
        if (this.storageType.equalsIgnoreCase("mysql")) {
            try {
                this.clearStaleLocks();
            }
            catch (SQLException e) {
                this.getLogger().warning("Could not clear locks: " + e.getMessage());
            }
        }
        this.sendConsole("\u00a7c________________________________________________________");
    }

    private void sendConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(Messages.parseCompatible(message));
    }

    private void createMessagesConfig() {
        this.messagesFile = new File(this.getDataFolder(), "messages.yml");
        if (!this.messagesFile.exists()) {
            this.saveResource("messages.yml", false);
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration((File)this.messagesFile);
        this.migrateMessageConfiguration();
    }

    private void migrateMessageConfiguration() {
        boolean messagesChanged = Messages.migrateLegacyValues(this.messagesConfig);
        if (messagesChanged) {
            try {
                this.messagesConfig.save(this.messagesFile);
            } catch (IOException exception) {
                this.getLogger().warning("Could not migrate messages.yml to MiniMessage: " + exception.getMessage());
            }
        }
        String blacklistMessage = this.getConfig().getString("blacklist.message");
        if (blacklistMessage != null && Messages.containsLegacyFormatting(blacklistMessage)) {
            this.getConfig().set("blacklist.message", Messages.migrateLegacy(blacklistMessage));
            this.saveConfig();
        }
    }

    private void createNamesConfig() {
        File oldFile = new File(this.getDataFolder(), "player_names.yml");
        this.namesFile = new File(this.dataFolder, "player_names.yml");
        if (oldFile.exists() && !this.namesFile.exists()) {
            oldFile.renameTo(this.namesFile);
        } else if (!this.namesFile.exists()) {
            try {
                this.namesFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.namesConfig = YamlConfiguration.loadConfiguration((File)this.namesFile);
    }

    private void createInvitesConfig() {
        this.invitesFile = new File(this.getDataFolder(), "invites.yml");
        if (!this.invitesFile.exists()) {
            try {
                this.invitesFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.invitesConfig = YamlConfiguration.loadConfiguration((File)this.invitesFile);
    }

    private void createSlotUpgradesConfig() {
        File oldFile = new File(this.getDataFolder(), "slot_upgrades.yml");
        this.slotUpgradesFile = new File(this.dataFolder, "slot_upgrades.yml");
        if (oldFile.exists() && !this.slotUpgradesFile.exists()) {
            oldFile.renameTo(this.slotUpgradesFile);
        } else if (!this.slotUpgradesFile.exists()) {
            try {
                this.slotUpgradesFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.slotUpgradesConfig = YamlConfiguration.loadConfiguration((File)this.slotUpgradesFile);
    }

    private void loadAdminAssignedData() {
        ConfigurationSection pagesSection;
        this.playerAssignedRows.clear();
        this.playerAssignedPages.clear();
        ConfigurationSection rowsSection = this.slotUpgradesConfig.getConfigurationSection("rows");
        if (rowsSection != null) {
            for (Object key : rowsSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString((String)key);
                    int rows = rowsSection.getInt((String)key, this.adminAssignedDefaultRows);
                    this.playerAssignedRows.put(uuid, Math.max(3, Math.min(6, rows)));
                }
                catch (IllegalArgumentException uuid) {}
            }
        }
        if ((pagesSection = this.slotUpgradesConfig.getConfigurationSection("pages")) != null) {
            for (Object key : pagesSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString((String)key);
                    int pages = pagesSection.getInt((String)key, this.adminAssignedDefaultPages);
                    this.playerAssignedPages.put(uuid, Math.max(1, pages));
                }
                catch (IllegalArgumentException uuid) {}
            }
        }
        this.cachedRankRows.clear();
        ConfigurationSection cachedRowsSection = this.slotUpgradesConfig.getConfigurationSection("cached-rank-rows");
        if (cachedRowsSection != null) {
            for (String key : cachedRowsSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int rows = cachedRowsSection.getInt(key, this.sizeConfigToRows(this.defaultSizeConfig));
                    this.cachedRankRows.put(uuid, Math.max(3, Math.min(6, rows)));
                }
                catch (IllegalArgumentException uuid) {}
            }
        }
        this.cachedPermissionPages.clear();
        ConfigurationSection cachedPagesSection = this.slotUpgradesConfig.getConfigurationSection("cached-permission-pages");
        if (cachedPagesSection != null) {
            for (String key : cachedPagesSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int pages = cachedPagesSection.getInt(key, 1);
                    this.cachedPermissionPages.put(uuid, Math.max(1, pages));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
        }
    }

    private void cachePermissionPages(UUID playerUUID, int pages) {
        Integer previous = this.cachedPermissionPages.put(playerUUID, pages);
        if (previous != null && previous == pages) {
            return;
        }
        this.slotUpgradesConfig.set("cached-permission-pages." + String.valueOf(playerUUID), (Object)pages);
        try {
            this.slotUpgradesConfig.save(this.slotUpgradesFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cacheRankRows(UUID playerUUID, int rows) {
        Integer previous = this.cachedRankRows.put(playerUUID, rows);
        if (previous != null && previous == rows) {
            return;
        }
        this.slotUpgradesConfig.set("cached-rank-rows." + String.valueOf(playerUUID), (Object)rows);
        try {
            this.slotUpgradesConfig.save(this.slotUpgradesFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAssignedRows(UUID playerUUID, int rows) {
        this.slotUpgradesConfig.set("rows." + playerUUID.toString(), (Object)Math.max(3, Math.min(6, rows)));
        try {
            this.slotUpgradesConfig.save(this.slotUpgradesFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAssignedPages(UUID playerUUID, int pages) {
        this.slotUpgradesConfig.set("pages." + playerUUID.toString(), (Object)Math.max(1, pages));
        try {
            this.slotUpgradesConfig.save(this.slotUpgradesFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeAssignedRows(UUID playerUUID) {
        this.slotUpgradesConfig.set("rows." + playerUUID.toString(), null);
        this.playerAssignedRows.remove(playerUUID);
        try {
            this.slotUpgradesConfig.save(this.slotUpgradesFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeAssignedPages(UUID playerUUID) {
        this.slotUpgradesConfig.set("pages." + playerUUID.toString(), null);
        this.playerAssignedPages.remove(playerUUID);
        try {
            this.slotUpgradesConfig.save(this.slotUpgradesFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getPlayerAssignedRows(UUID playerUUID) {
        if (!this.adminAssignedRowsEnabled) {
            return this.adminAssignedDefaultRows;
        }
        return this.playerAssignedRows.getOrDefault(playerUUID, this.adminAssignedDefaultRows);
    }

    private int getPlayerAssignedPages(UUID playerUUID) {
        if (!this.adminAssignedPagesEnabled) {
            return this.adminAssignedDefaultPages;
        }
        return this.playerAssignedPages.getOrDefault(playerUUID, this.adminAssignedDefaultPages);
    }

    private List<String> searchItemsInEnderChest(UUID ownerId, String searchTerm) {
        ArrayList<String> results;
        block4: {
            block3: {
                results = new ArrayList<String>();
                if (!this.pagesEnabled) break block3;
                int maxPages = this.getPlayerMaxPages(ownerId);
                for (int page = 1; page <= maxPages; ++page) {
                    ItemStack[] items = this.loadPageItems(ownerId, page);
                    for (int i = 0; i < items.length; ++i) {
                        String displayName;
                        ItemStack item = items[i];
                        if (item == null || item.getType() == Material.AIR) continue;
                        String itemName = item.getType().toString().replace("_", " ").toLowerCase();
                        String string = displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? ChatColor.stripColor((String)item.getItemMeta().getDisplayName()).toLowerCase() : "";
                        if (!itemName.contains(searchTerm) && !displayName.contains(searchTerm)) continue;
                        String friendlyName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "\u00a77" + itemName;
                        results.add("\u00a7dPage " + page + " \u00a77Slot " + (i + 1) + ": " + friendlyName + " \u00a77x" + item.getAmount());
                    }
                }
                break block4;
            }
            ItemStack[] items = this.loadItemsFromStorage(ownerId);
            if (items == null) break block4;
            for (int i = 0; i < items.length; ++i) {
                String displayName;
                ItemStack item = items[i];
                if (item == null || item.getType() == Material.AIR) continue;
                String itemName = item.getType().toString().replace("_", " ").toLowerCase();
                String string = displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? ChatColor.stripColor((String)item.getItemMeta().getDisplayName()).toLowerCase() : "";
                if (!itemName.contains(searchTerm) && !displayName.contains(searchTerm)) continue;
                String friendlyName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "\u00a77" + itemName;
                results.add("\u00a77Slot " + (i + 1) + ": " + friendlyName + " \u00a77x" + item.getAmount());
            }
        }
        return results;
    }

    String getPageStorageKey(UUID ownerId, int page) {
        if (page <= 1) {
            return ownerId.toString();
        }
        return ownerId.toString() + "_page" + page;
    }

    private int computeOwnerChestRows(UUID ownerId) {
        Player ownerPlayer = Bukkit.getPlayer((UUID)ownerId);
        if (ownerPlayer != null) {
            return this.getChestRows(ownerPlayer, false);
        }
        if (this.adminAssignedRowsEnabled && this.playerAssignedRows.containsKey(ownerId)) {
            return this.getPlayerAssignedRows(ownerId);
        }
        if (this.cachedRankRows.containsKey(ownerId)) {
            return this.cachedRankRows.get(ownerId);
        }
        return this.sizeConfigToRows(this.defaultSizeConfig);
    }

    private int getPlayerMaxPages(Player player) {
        if (!this.pagesEnabled) {
            return 1;
        }
        int max = 1;
        if (this.adminAssignedPagesEnabled) {
            max = this.getPlayerAssignedPages(player.getUniqueId());
        }
        int permissionPages = 1;
        for (int i = this.maxPages; i > 1; --i) {
            if (!player.hasPermission("ec.pages." + i)) continue;
            permissionPages = i;
            break;
        }
        this.cachePermissionPages(player.getUniqueId(), permissionPages);
        if (permissionPages > max) {
            max = permissionPages;
        }
        return Math.min(max, this.maxPages);
    }

    private int getPlayerMaxPages(UUID ownerId) {
        if (!this.pagesEnabled) {
            return 1;
        }
        Player p = Bukkit.getPlayer((UUID)ownerId);
        if (p != null) {
            return this.getPlayerMaxPages(p);
        }
        int max = this.adminAssignedPagesEnabled ? this.getPlayerAssignedPages(ownerId) : this.maxPages;
        Integer cached = this.cachedPermissionPages.get(ownerId);
        if (cached != null && cached > max) {
            max = cached;
        }
        return Math.min(Math.max(1, max), this.maxPages);
    }

    private boolean isItemBlacklisted(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        if (this.blacklistedItems.isEmpty()) {
            return false;
        }
        return this.blacklistedItems.contains(item.getType());
    }

    private void loadConfigValues() {
        this.reloadConfig();
        this.getConfig().addDefault("backups.enabled", (Object)true);
        this.getConfig().addDefault("backups.auto-enabled", (Object)false);
        this.getConfig().addDefault("backups.auto-time", (Object)"04:00");
        this.getConfig().addDefault("backups.max-backups", (Object)15);
        this.getConfig().addDefault("backups.chat-announcements", (Object)true);
        this.getConfig().addDefault("backups.safety-backup-before-restore", (Object)true);
        this.getConfig().addDefault("backups.webhook.enabled", (Object)false);
        this.getConfig().addDefault("backups.webhook.url", (Object)"");
        this.getConfig().addDefault("backups.webhook.notify-on-success", (Object)false);
        this.getConfig().addDefault("opening.cooldown-seconds", (Object)2.0);
        this.getConfig().addDefault("permissions.require-use-permission-for-command", (Object)true);
        this.getConfig().addDefault("autosave.enabled", (Object)true);
        this.getConfig().addDefault("autosave.interval-seconds", (Object)60);
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        if (this.messagesFile == null) {
            this.messagesFile = new File(this.getDataFolder(), "messages.yml");
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration((File)this.messagesFile);
        this.migrateMessageConfiguration();
        this.invitesConfig = YamlConfiguration.loadConfiguration((File)this.invitesFile);
        this.defaultSizeConfig = this.getConfig().getInt("size", 1);
        this.rankSizeEnabled = this.getConfig().getBoolean("ranks.enabled", false);
        this.rankSizes.clear();
        if (this.rankSizeEnabled && this.getConfig().getConfigurationSection("ranks.groups") != null) {
            ConfigurationSection sec = this.getConfig().getConfigurationSection("ranks.groups");
            for (String key : sec.getKeys(false)) {
                this.rankSizes.put(key, sec.getInt(key));
            }
        }
        this.teamsEnabled = this.getConfig().getBoolean("teams.enabled", true);
        this.teamSizeConfig = this.getConfig().getInt("teams.size", 2);
        this.showOpenMessage = this.getConfig().getBoolean("messages.show-open-message", true);
        this.showCloseMessage = this.getConfig().getBoolean("messages.show-close-message", true);
        this.openSound = this.getConfig().getString("sounds.open", "BLOCK_ENDER_CHEST_OPEN");
        this.closeSound = this.getConfig().getString("sounds.close", "BLOCK_ENDER_CHEST_CLOSE");
        this.storageType = this.getConfig().getString("storage-type", "file");
        this.host = this.getConfig().getString("mysql.host");
        this.port = this.getConfig().getInt("mysql.port");
        this.database = this.getConfig().getString("mysql.database");
        this.username = this.getConfig().getString("mysql.username");
        this.password = this.getConfig().getString("mysql.password");
        this.table = this.getConfig().getString("mysql.table", "enderchest_data");
        this.useSSL = this.getConfig().getBoolean("mysql.use-ssl", false);
        this.serverId = this.normalizeServerId(this.getConfig().getString("mysql.server-id", "server1"));
        if (this.serverId.isEmpty()) {
            this.serverId = "server1";
        }
        this.requireUsePermission = this.getConfig().getBoolean("permissions.require-use-permission", false);
        this.requireUsePermissionForCommand = this.getConfig().getBoolean("permissions.require-use-permission-for-command", true);
        this.enableBlockPermission = this.getConfig().getBoolean("permissions.enable-block-permission", true);
        this.openCooldownMs = Math.max(0L, Math.round(this.getConfig().getDouble("opening.cooldown-seconds", 2.0) * 1000.0));
        this.blacklistedItems.clear();
        if (this.getConfig().getBoolean("blacklist.enabled", false)) {
            List<String> blacklistStrings = this.getConfig().getStringList("blacklist.items");
            for (String matName : blacklistStrings) {
                try {
                    Material mat = Material.valueOf((String)matName.toUpperCase());
                    this.blacklistedItems.add(mat);
                }
                catch (IllegalArgumentException e) {
                    this.getLogger().warning("Invalid blacklist material: " + matName);
                }
            }
        }
        this.blacklistMessage = this.getConfig().getString("blacklist.message", "&cThis item cannot be stored in the EnderChest.");
        this.pagesEnabled = this.getConfig().getBoolean("pages.enabled", false);
        this.maxPages = Math.max(1, this.getConfig().getInt("pages.max-pages", 3));
        this.adminAssignedRowsEnabled = this.getConfig().getBoolean("admin-assigned-rows.enabled", true);
        this.adminAssignedDefaultRows = Math.max(3, Math.min(6, this.getConfig().getInt("admin-assigned-rows.default-rows", 3)));
        this.adminAssignedMaxRows = Math.max(3, Math.min(6, this.getConfig().getInt("admin-assigned-rows.max-rows", 6)));
        this.adminAssignedPagesEnabled = this.getConfig().getBoolean("admin-assigned-pages.enabled", true);
        this.adminAssignedDefaultPages = Math.max(1, this.getConfig().getInt("admin-assigned-pages.default-pages", 1));
        this.adminAssignedMaxPages = Math.max(1, this.getConfig().getInt("admin-assigned-pages.max-pages", 5));
        this.loadAdminAssignedData();
        this.autosaveEnabled = this.getConfig().getBoolean("autosave.enabled", true);
        this.autosaveIntervalSeconds = this.getConfig().getInt("autosave.interval-seconds", 60);
        this.startAutosaveTask();
        if (this.backupManager != null) {
            this.backupManager.reload();
        }
    }

    private boolean passOpenCooldown(Player viewer) {
        long elapsed;
        if (this.openCooldownMs <= 0L) {
            return true;
        }
        long now = System.currentTimeMillis();
        UUID viewerId = viewer.getUniqueId();
        Long lastAttempt = this.lastOpenAttemptMs.get(viewerId);
        if (lastAttempt != null && (elapsed = now - lastAttempt) < this.openCooldownMs) {
            double remaining = (double)(this.openCooldownMs - elapsed) / 1000.0;
            String seconds = String.format(Locale.US, "%.1f", remaining);
            viewer.sendMessage(this.getPrefix() + this.getRawMessage("open-cooldown", "&cPlease wait &f{seconds}s &cbefore opening your EnderChest again.").replace("{seconds}", seconds));
            return false;
        }
        this.lastOpenAttemptMs.put(viewerId, now);
        return true;
    }

    String getMessage(String key) {
        String msg = this.messagesConfig.getString(key, "&cMessage missing: " + key);
        return Messages.legacy(this.getPrefix() + msg);
    }

    String getMessage(String key, String def) {
        String msg = this.messagesConfig.getString(key, def);
        return Messages.legacy(this.getPrefix() + msg);
    }

    private boolean hasBlockPermission(Player player) {
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getPermission().equalsIgnoreCase("ec.block")) continue;
            return info.getValue();
        }
        return false;
    }

    private boolean canOpenPhysicalEnderChest(Player player) {
        if (this.enableBlockPermission && this.hasBlockPermission(player)) {
            player.sendMessage(this.getMessage("no-access"));
            return false;
        }
        if (this.requireUsePermission && !player.hasPermission("ec.use")) {
            player.sendMessage(this.getMessage("no-permission"));
            return false;
        }
        return true;
    }

    private boolean canUseVirtualEnderChestCommand(Player player) {
        if (this.enableBlockPermission && this.hasBlockPermission(player)) {
            player.sendMessage(this.getMessage("no-access"));
            return false;
        }
        if (this.requireUsePermissionForCommand && !player.hasPermission("ec.use")) {
            player.sendMessage(this.getMessage("no-permission"));
            return false;
        }
        return true;
    }

    private boolean hasVanillaBypass(Player player) {
        for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getPermission().equalsIgnoreCase("ec.vanilla")) continue;
            return info.getValue();
        }
        return false;
    }

    private String getRawMessage(String key, String fallback) {
        return Messages.legacy(this.messagesConfig.getString(key, fallback));
    }

    private String getInventoryTitle() {
        return this.getRawMessage("inventory-title", "Ender Chest");
    }

    private String getPagedInventoryTitle(int page, int maxPages) {
        return this.getRawMessage("inventory-title-paged", "Ender Chest - Page {page}/{pages}").replace("{page}", String.valueOf(page)).replace("{pages}", String.valueOf(maxPages));
    }

    String getPrefix() {
        return this.messagesConfig.getString("prefix", "<dark_purple>EnderChest <gray>>> ");
    }

    String prefixed(String message) {
        return Messages.legacy(this.getPrefix() + message);
    }

    boolean shouldAnnounceBackupMessages() {
        return this.getConfig().getBoolean("backups.chat-announcements", true);
    }

    private int getChestRows(Player p, boolean isTeam) {
        if (isTeam) {
            return this.sizeConfigToRows(this.teamSizeConfig);
        }
        if (this.adminAssignedRowsEnabled && this.playerAssignedRows.containsKey(p.getUniqueId())) {
            return this.getPlayerAssignedRows(p.getUniqueId());
        }
        int rows = this.sizeConfigToRows(this.defaultSizeConfig);
        if (this.rankSizeEnabled) {
            for (Map.Entry<String, Integer> entry : this.rankSizes.entrySet()) {
                int r;
                String rank = entry.getKey();
                int sizeId = entry.getValue();
                if (!p.hasPermission("group." + rank) || (r = this.sizeConfigToRows(sizeId)) <= rows) continue;
                rows = r;
            }
        }
        this.cacheRankRows(p.getUniqueId(), rows);
        return rows;
    }

    private int sizeConfigToRows(int v) {
        if (v <= 1) {
            return 3;
        }
        if (v == 2) {
            return 6;
        }
        return Math.max(3, Math.min(6, v));
    }

    private InviteAccess parseInviteAccess(String rawEntry) {
        if (rawEntry == null || rawEntry.isBlank()) {
            return null;
        }
        try {
            String[] parts = rawEntry.split("\\|", 2);
            UUID targetId = UUID.fromString(parts[0]);
            if (parts.length == 1) {
                return new InviteAccess(targetId, false, 1);
            }
            String scope = parts[1].trim().toLowerCase(Locale.ROOT);
            if (INVITE_SCOPE_ALL.equals(scope)) {
                return new InviteAccess(targetId, true, null);
            }
            if (scope.startsWith(INVITE_SCOPE_PAGE_PREFIX)) {
                int page = Integer.parseInt(scope.substring(INVITE_SCOPE_PAGE_PREFIX.length()));
                return new InviteAccess(targetId, false, Math.max(1, page));
            }
            return new InviteAccess(targetId, false, 1);
        }
        catch (Exception ignored) {
            return null;
        }
    }

    private String createInviteEntry(UUID target, Integer page, boolean allPages) {
        if (allPages) {
            return String.valueOf(target) + "|all";
        }
        if (page == null || page <= 1) {
            return target.toString();
        }
        return String.valueOf(target) + "|page:" + page;
    }

    private List<String> getInviteEntries(UUID owner) {
        return new ArrayList<String>(this.invitesConfig.getStringList(owner.toString()));
    }

    private boolean removeInviteEntries(List<String> list, UUID target) {
        return list.removeIf(entry -> {
            InviteAccess access = this.parseInviteAccess((String)entry);
            return access != null && access.target.equals(target);
        });
    }

    private boolean hasInviteAccess(UUID owner, UUID viewer, Integer requestedPage) {
        if (owner.equals(viewer)) {
            return true;
        }
        for (String rawEntry : this.getInviteEntries(owner)) {
            InviteAccess access = this.parseInviteAccess(rawEntry);
            if (access == null || !access.target.equals(viewer)) continue;
            if (requestedPage == null || !this.pagesEnabled) {
                return true;
            }
            if (access.allPages) {
                return true;
            }
            if (access.page == null || access.page != requestedPage) continue;
            return true;
        }
        return false;
    }

    private boolean saveInvite(UUID owner, UUID target, Integer page, boolean allPages) {
        List<String> list = this.getInviteEntries(owner);
        String newEntry = this.createInviteEntry(target, page, allPages);
        for (String rawEntry : list) {
            int requested;
            InviteAccess access = this.parseInviteAccess(rawEntry);
            if (access == null || !access.target.equals(target)) continue;
            if (access.allPages && allPages) {
                return false;
            }
            if (access.allPages || allPages) break;
            int existingPage = access.page == null ? 1 : access.page;
            if (existingPage != (requested = page == null ? 1 : page)) continue;
            return false;
        }
        this.removeInviteEntries(list, target);
        list.add(newEntry);
        this.invitesConfig.set(owner.toString(), list);
        try {
            this.invitesConfig.save(this.invitesFile);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveInvite(UUID owner, UUID target) {
        this.saveInvite(owner, target, 1, false);
    }

    private void removeInvite(UUID owner, UUID target) {
        List<String> list = this.getInviteEntries(owner);
        if (this.removeInviteEntries(list, target)) {
            this.invitesConfig.set(owner.toString(), list);
            try {
                this.invitesConfig.save(this.invitesFile);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isInvited(UUID owner, UUID viewer) {
        return this.hasInviteAccess(owner, viewer, null);
    }

    private boolean isInvitedForPage(UUID owner, UUID viewer, int page) {
        return this.hasInviteAccess(owner, viewer, page);
    }

    private List<String> getInviteDescriptions(UUID owner) {
        ArrayList<String> names = new ArrayList<String>();
        for (String rawEntry : this.getInviteEntries(owner)) {
            String name;
            InviteAccess access = this.parseInviteAccess(rawEntry);
            if (access == null) continue;
            OfflinePlayer op = Bukkit.getOfflinePlayer((UUID)access.target);
            String string = name = op.getName() != null ? op.getName() : access.target.toString().substring(0, 8);
            if (!this.pagesEnabled) {
                names.add(name);
                continue;
            }
            if (access.allPages) {
                names.add(name + " (all pages)");
                continue;
            }
            int page = access.page == null ? 1 : access.page;
            names.add(name + " (page " + page + ")");
        }
        return names;
    }

    UUID getTeamChestId(String teamName) {
        return UUID.nameUUIDFromBytes(("Team:" + teamName).getBytes());
    }

    private boolean isTeamChestMember(Player player, UUID ownerId) {
        if (!this.teamsEnabled) {
            return false;
        }
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        if (team == null) {
            return false;
        }
        return ownerId.equals(this.getTeamChestId(team.getName()));
    }

    void openEnderChestLogic(Player viewer, UUID ownerId, String ownerName, boolean isTeam) {
        this.openEnderChestLogic(viewer, ownerId, ownerName, isTeam, 1);
    }

    private void openEnderChestLogic(Player viewer, UUID ownerId, String ownerName, boolean isTeam, int page) {
        UUID currentOwnerId;
        int currentPage;
        int requestedPage;
        if (this.isMigrating) {
            viewer.sendMessage(this.prefixed("&cA migration, backup, or restore is currently in progress. Please wait."));
            return;
        }
        UUID viewerId = viewer.getUniqueId();
        if (this.switchingPages.contains(viewerId)) {
            viewer.sendMessage(this.prefixed("&cPlease wait, your EnderChest page is still switching..."));
            return;
        }
        int ownerMaxPages = isTeam ? 1 : this.getPlayerMaxPages(ownerId);
        boolean usePages = this.pagesEnabled && ownerMaxPages > 1 && !isTeam;
        int n = requestedPage = usePages ? Math.max(1, Math.min(page, ownerMaxPages)) : 1;
        if (!(isTeam || ownerId.equals(viewerId) || viewer.hasPermission("ec.see"))) {
            boolean allowed;
            boolean bl = allowed = usePages ? this.inviteService.hasAccess(ownerId, viewerId, requestedPage) : this.inviteService.hasAccess(ownerId, viewerId, null);
            if (!allowed) {
                viewer.sendMessage(this.getMessage("share-not-invited").replace("{player}", ownerName));
                return;
            }
        }
        int n2 = currentPage = (currentOwnerId = this.viewerToOwnerMap.get(viewer.getUniqueId())) == null ? 1 : this.playerCurrentPage.getOrDefault(viewerId, this.activeInventoryPage.getOrDefault(currentOwnerId, 1));
        if (currentOwnerId != null) {
            Inventory openInv = viewer.getOpenInventory().getTopInventory();
            if (!this.isEnderChestInventory(openInv)) {
                this.getLogger().warning("[Self-Heal] Player " + viewer.getName() + " had desynced EC session. Clearing state.");
                Inventory strandedInv = this.activeInventories.get(currentOwnerId);
                EnderChestHolder strandedHolder = this.holderOf(strandedInv);
                if (strandedHolder != null && strandedInv.getViewers().isEmpty()) {
                    this.activeInventories.remove(currentOwnerId, strandedInv);
                    this.persistWindow(strandedHolder, strandedInv.getContents());
                }
                this.viewerToOwnerMap.remove(viewerId);
                this.playerCurrentPage.remove(viewerId);
                currentOwnerId = null;
            } else {
                if (currentOwnerId.equals(ownerId)) {
                    if (usePages && requestedPage != currentPage) {
                        if (!this.passOpenCooldown(viewer)) {
                            return;
                        }
                        this.saveCurrentPageAndSwitch(viewer, ownerId, currentPage, requestedPage);
                        return;
                    }
                    viewer.sendMessage(this.getMessage("already-viewing"));
                } else {
                    viewer.sendMessage(this.prefixed("&cPlease close your current EnderChest first."));
                }
                return;
            }
        }
        if (!this.passOpenCooldown(viewer)) {
            return;
        }
        if (this.pendingViewerOpens.contains(viewerId)) {
            viewer.sendMessage(this.prefixed("&cPlease wait, your EnderChest is still opening..."));
            return;
        }
        if (!isTeam) {
            this.knownPlayerNames.add(ownerName);
        }
        int computedRows = isTeam ? this.sizeConfigToRows(this.teamSizeConfig) : this.computeOwnerChestRows(ownerId);
        int pageRows = Math.max(3, Math.min(6, computedRows));
        if ((!usePages || this.activeInventoryPage.getOrDefault(ownerId, 1) == requestedPage) && this.reopenExistingInventory(viewer, ownerId)) {
            return;
        }
        Inventory otherActive = this.activeInventories.get(ownerId);
        if (otherActive != null && !otherActive.getViewers().isEmpty() && this.activeInventoryPage.getOrDefault(ownerId, 1) != requestedPage) {
            viewer.sendMessage(this.prefixed("&cThis EnderChest is currently open on a different page. Please try again shortly."));
            return;
        }
        if (this.pendingSaves.contains(ownerId)) {
            viewer.sendMessage(this.prefixed("&cThis EnderChest is still saving. Please try again in a moment."));
            return;
        }
        if (this.pendingLoads.contains(ownerId)) {
            viewer.sendMessage(this.prefixed("&cThis EnderChest is already loading..."));
            return;
        }
        this.pendingViewerOpens.add(viewerId);
        this.pendingLoads.add(ownerId);
        this.foliaLib.getScheduler().runAsync(task -> {
            if (!this.tryLockChest(ownerId)) {
                this.foliaLib.getScheduler().runAtEntity((Entity)viewer, task2 -> {
                    this.pendingLoads.remove(ownerId);
                    this.pendingViewerOpens.remove(viewerId);
                    viewer.sendMessage(this.getMessage("chest-locked"));
                });
                return;
            }
            this.startLockTask(ownerId);
            ItemStack[] items = usePages ? this.loadPageItems(ownerId, requestedPage) : this.loadItemsFromStorage(ownerId);
            this.foliaLib.getScheduler().runAtEntity((Entity)viewer, task3 -> {
                this.pendingLoads.remove(ownerId);
                if (!viewer.isOnline()) {
                    this.loadFailures.remove(ownerId);
                    this.pendingViewerOpens.remove(viewerId);
                    this.stopLockTask(ownerId);
                    this.unlockChest(ownerId);
                    return;
                }
                if (this.isMigrating) {
                    this.pendingViewerOpens.remove(viewerId);
                    this.stopLockTask(ownerId);
                    this.unlockChest(ownerId);
                    viewer.sendMessage(this.prefixed("&cA migration, backup, or restore just started. Please try again."));
                    return;
                }
                if (this.loadFailures.remove(ownerId)) {
                    this.pendingViewerOpens.remove(viewerId);
                    this.stopLockTask(ownerId);
                    this.unlockChest(ownerId);
                    viewer.sendMessage(this.prefixed("&cYour EnderChest could not be loaded. This is usually caused by an enchantment or item from a disabled plugin. Ask an admin to re-enable the plugin, or to recover the chest with /ec repair."));
                    return;
                }
                if ((!usePages || this.activeInventoryPage.getOrDefault(ownerId, 1) == requestedPage) && this.reopenExistingInventory(viewer, ownerId)) {
                    this.pendingViewerOpens.remove(viewerId);
                    return;
                }
                String title = isTeam ? this.getRawMessage("team-title", "&dTeam: &5{team}").replace("{team}", ownerName) : (usePages ? this.getPagedInventoryTitle(requestedPage, ownerMaxPages) : this.getInventoryTitle());
                Inventory alreadyOpen = this.activeInventories.get(ownerId);
                if (alreadyOpen != null && this.reopenExistingInventory(viewer, ownerId)) {
                    this.pendingViewerOpens.remove(viewerId);
                    return;
                }
                EnderChestHolder holder = new EnderChestHolder(ownerId, ownerName, usePages ? requestedPage : 1, this.nextGeneration(ownerId), isTeam, usePages);
                Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)(pageRows * 9), (String)title);
                if (inv == null && (inv = Bukkit.createInventory((InventoryHolder)holder, (int)(pageRows * 9))) == null) {
                    this.pendingViewerOpens.remove(viewerId);
                    this.stopLockTask(ownerId);
                    this.unlockChest(ownerId);
                    viewer.sendMessage(this.prefixed("&cThe EnderChest could not be opened right now."));
                    return;
                }
                holder.setInventory(inv);
                if (usePages) {
                    this.playerCurrentPage.put(viewer.getUniqueId(), requestedPage);
                    this.activeInventoryPage.put(ownerId, requestedPage);
                } else {
                    this.activeInventoryPage.remove(ownerId);
                }
                int storageSize = pageRows * 9;
                ArrayList<ItemStack> overflowItems = new ArrayList<ItemStack>();
                if (items != null && items.length > 0) {
                    if (items.length > storageSize) {
                        for (int i = storageSize; i < items.length; ++i) {
                            if (items[i] == null || items[i].getType() == Material.AIR) continue;
                            overflowItems.add(items[i]);
                        }
                        for (int i = 0; i < Math.min(items.length, storageSize); ++i) {
                            inv.setItem(i, items[i]);
                        }
                    } else {
                        for (int i = 0; i < items.length; ++i) {
                            inv.setItem(i, items[i]);
                        }
                    }
                    if (this.showOpenMessage && !isTeam) {
                        viewer.sendMessage(this.getMessage("chest-loaded").replace("{player}", ownerName));
                    }
                } else if (this.showOpenMessage && !isTeam) {
                    viewer.sendMessage(this.getMessage("chest-empty").replace("{player}", ownerName));
                }
                viewer.closeInventory();
                this.activeInventories.put(ownerId, inv);
                if (!this.safeOpenInventory(viewer, inv)) {
                    this.pendingViewerOpens.remove(viewerId);
                    this.activeInventories.remove(ownerId);
                    this.stopLockTask(ownerId);
                    this.unlockChest(ownerId);
                    return;
                }
                this.viewerToOwnerMap.put(viewer.getUniqueId(), ownerId);
                this.playSound(viewer, this.openSound);
                if (!overflowItems.isEmpty()) {
                    for (ItemStack overflow : overflowItems) {
                        viewer.getWorld().dropItem(viewer.getLocation(), overflow);
                    }
                    viewer.sendMessage(this.getMessage("size-reduced-online").replace("{count}", String.valueOf(overflowItems.size())));
                }
                this.pendingViewerOpens.remove(viewerId);
            });
        });
    }

    void handleEcSeeTarget(Player p, UUID targetUUID, String realName, String[] args) {
        int page = 1;
        if (args.length == 2) {
            if (!this.pagesEnabled) {
                p.sendMessage(this.prefixed("&cPages are currently disabled."));
                return;
            }
            try {
                page = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                p.sendMessage(this.prefixed("&cPage must be a number."));
                return;
            }
            int max = this.getPlayerMaxPages(targetUUID);
            if (page < 1 || page > max) {
                p.sendMessage(this.prefixed("&cPage must be between 1 and " + max + "."));
                return;
            }
        }
        this.openEnderChestLogic(p, targetUUID, realName, false, page);
    }

    private void handleUninviteTarget(Player p, UUID targetUUID, String targetName) {
        if (!this.inviteService.hasAccess(p.getUniqueId(), targetUUID, null)) {
            p.sendMessage(this.getMessage("invite-not-invited"));
        } else {
            this.inviteService.remove(p.getUniqueId(), targetUUID);
            p.sendMessage(this.getMessage("invite-removed").replace("{player}", targetName));
        }
    }

    private void handleClearTarget(CommandSender sender, UUID targetUUID, String realName, String[] args) {
        int page;
        if (args.length == 2) {
            this.clearPlayerData(targetUUID, null);
            sender.sendMessage(this.prefixed("&aSuccessfully cleared all EnderChest pages for &f" + realName + "&a."));
            return;
        }
        if (!this.pagesEnabled) {
            sender.sendMessage(this.prefixed("&cPages are currently disabled."));
            return;
        }
        try {
            page = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(this.prefixed("&cPage must be a number."));
            return;
        }
        if (page < 1) {
            sender.sendMessage(this.prefixed("&cPage must be 1 or higher."));
            return;
        }
        this.clearPlayerData(targetUUID, page);
        sender.sendMessage(this.prefixed("&aSuccessfully cleared page &f" + page + " &aof &f" + realName + "'s &aEnderChest."));
    }

    private void sendPageInfo(CommandSender sender, UUID targetUUID, String targetName) {
        int pages = this.getPlayerAssignedPages(targetUUID);
        boolean isCustom = this.playerAssignedPages.containsKey(targetUUID);
        sender.sendMessage(this.prefixed("&f" + targetName + " &7has &f" + pages + " pages" + (isCustom ? " &a(custom)" : " &7(default)")));
        int effective = this.getPlayerMaxPages(targetUUID);
        if (effective != pages) {
            boolean online = Bukkit.getPlayer((UUID)targetUUID) != null;
            sender.sendMessage(this.prefixed("&7Effective: &f" + effective + " pages &7(incl. permissions" + (online ? "" : ", cached from last login") + ")"));
        }
    }

    private void sendRowInfo(CommandSender sender, UUID targetUUID, String targetName) {
        int rows = this.getPlayerAssignedRows(targetUUID);
        boolean isCustom = this.playerAssignedRows.containsKey(targetUUID);
        sender.sendMessage(this.prefixed("&f" + targetName + " &7has &f" + rows + " rows" + (isCustom ? " &a(custom)" : " &7(default)")));
    }

    void handleEcShareTarget(Player p, UUID targetUUID, String realName, int requestedPage) {
        boolean allowed;
        if (targetUUID.equals(p.getUniqueId())) {
            if (!this.canUseVirtualEnderChestCommand(p)) {
                return;
            }
            this.openEnderChestLogic(p, p.getUniqueId(), p.getName(), false, requestedPage);
            return;
        }
        int ownerMaxPages = this.getPlayerMaxPages(targetUUID);
        if (this.pagesEnabled && requestedPage > ownerMaxPages) {
            p.sendMessage(this.prefixed("&cPage must be between 1 and " + ownerMaxPages + "."));
            return;
        }
        boolean bl = allowed = this.pagesEnabled && ownerMaxPages > 1 ? this.inviteService.hasAccess(targetUUID, p.getUniqueId(), requestedPage) : this.inviteService.hasAccess(targetUUID, p.getUniqueId(), null);
        if (allowed || p.hasPermission("ec.see")) {
            this.openEnderChestLogic(p, targetUUID, realName, false, requestedPage);
        } else {
            p.sendMessage(this.getMessage("share-not-invited").replace("{player}", realName));
        }
    }

    void resolveOfflinePlayerByName(CommandSender sender, String name, Consumer<OfflinePlayer> callback) {
        this.foliaLib.getScheduler().runAsync(task -> {
            OfflinePlayer result = Bukkit.getOfflinePlayer((String)name);
            if (sender instanceof Player) {
                Player player = (Player)sender;
                this.foliaLib.getScheduler().runAtEntity((Entity)player, task2 -> {
                    if (player.isOnline()) {
                        callback.accept(result);
                    }
                });
            } else {
                this.foliaLib.getScheduler().runNextTick(task2 -> callback.accept(result));
            }
        });
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (this.isMigrating) {
            sender.sendMessage(this.prefixed("&cA migration, backup, or restore is already running."));
            return true;
        }
        if (command.getName().equalsIgnoreCase("teamec")) {
            return this.teamEnderChestCommand.execute(sender);
        }
        if (command.getName().equalsIgnoreCase("ecshare")) {
            return this.ecShareCommand.execute(sender, args);
        }
        if (command.getName().equalsIgnoreCase("ec")) {
            String sub;
            Player p;
            block160: {
                if (sender instanceof Player && this.hasVanillaBypass(p = (Player)sender)) {
                    boolean isAdminCmd;
                    if (args.length == 0) {
                        p.openInventory(p.getEnderChest());
                        this.playSound(p, this.openSound);
                        return true;
                    }
                    sub = args[0].toLowerCase();
                    boolean bl = isAdminCmd = sub.equals("reload") || sub.equals("migrate") || sub.equals("export") || sub.equals("backup") || sub.equals("autobackup") || sub.equals("backupannounce") || sub.equals("setrows") || sub.equals("resetrows") || sub.equals("rowinfo") || sub.equals("setpages") || sub.equals("resetpages") || sub.equals("pageinfo") || sub.equals("clear") || sub.equals("clearall") || sub.equals("repair");
                    if (!isAdminCmd) {
                        p.sendMessage(this.getMessage("vanilla-bypass"));
                        return true;
                    }
                }
                if (args.length >= 1) {
                    String sub2 = args[0].toLowerCase();
                    if (sub2.equals("help")) {
                        boolean isAdmin = sender.hasPermission("ec.admin");
                        sender.sendMessage("\u00a75\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501 \u00a7d\u00a7lEnderChest \u00a7r\u00a77v" + this.getDescription().getVersion() + " \u00a75\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                        sender.sendMessage("\u00a7d/ec \u00a78- \u00a77Open your personal Ender Chest.");
                        if (this.pagesEnabled) {
                            sender.sendMessage("\u00a7d/ec page \u00a7e<number> \u00a78- \u00a77Open a specific page of your Ender Chest.");
                        }
                        sender.sendMessage("\u00a7d/ec search \u00a7e<item> \u00a78- \u00a77Search for items in your Ender Chest.");
                        sender.sendMessage("\u00a7d/ec invite \u00a7e<player> \u00a78- \u00a77Share page 1 or your standard chest.");
                        if (this.pagesEnabled) {
                            sender.sendMessage("\u00a7d/ec invite \u00a7e<all|number> <player> \u00a78- \u00a77Share all pages or one specific page.");
                        }
                        sender.sendMessage("\u00a7d/ec uninvite \u00a7e<player> \u00a78- \u00a77Revoke a player's access.");
                        sender.sendMessage("\u00a7d/ec invitelist \u00a78- \u00a77List all invited players.");
                        sender.sendMessage("\u00a7d/ecshare \u00a7e<player> [page] \u00a78- \u00a77Open a chest page you were invited to.");
                        if (this.teamsEnabled) {
                            sender.sendMessage("\u00a7d/teamec \u00a78- \u00a77Open the shared team chest.");
                        }
                        if (sender.hasPermission("ec.see")) {
                            sender.sendMessage("\u00a7d/ecsee \u00a7e<player> \u00a78- \u00a77[Admin] Inspect any player's chest.");
                        }
                        if (isAdmin) {
                            sender.sendMessage("\u00a75\u00a7l\u2500\u2500 \u00a7dAdmin Commands \u00a75\u00a7l\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
                            sender.sendMessage("\u00a7d/ec reload \u00a78- \u00a77Reload the config.");
                            sender.sendMessage("\u00a7d/ec backup \u00a78- \u00a77Open the backup GUI.");
                            sender.sendMessage("\u00a7d/ec autobackup <on|off> [HH:mm] \u00a78- \u00a77Toggle scheduled backups.");
                            sender.sendMessage("\u00a7d/ec backupannounce <on|off> \u00a78- \u00a77Toggle chat announcements for backup actions.");
                            sender.sendMessage("\u00a7d/ec repair \u00a78- \u00a77Recover chests that refuse to open (\u00a7d/ec repair\u00a77 for details).");
                            sender.sendMessage("");
                            sender.sendMessage("\u00a75\u00a7l\u2500\u2500 \u00a7dSize & Page Management \u00a75\u00a7l\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500");
                            sender.sendMessage("\u00a7d/ec setrows \u00a7e<player> \u00a7e<3-6> \u00a78- \u00a77Set player's EnderChest rows.");
                            sender.sendMessage("\u00a7d/ec resetrows \u00a7e<player> \u00a78- \u00a77Reset player's rows to default.");
                            sender.sendMessage("\u00a7d/ec rowinfo \u00a7e[player] \u00a78- \u00a77View row assignments.");
                            sender.sendMessage("\u00a7d/ec setpages \u00a7e<player> \u00a7e<1-" + this.maxPages + "> \u00a78- \u00a77Set player's pages.");
                            sender.sendMessage("\u00a7d/ec resetpages \u00a7e<player> \u00a78- \u00a77Reset player's pages to default.");
                            sender.sendMessage("\u00a7d/ec pageinfo \u00a7e[player] \u00a78- \u00a77View page assignments.");
                            sender.sendMessage("");
                            sender.sendMessage("\u00a7e\u00a7lMigration Guide:");
                            sender.sendMessage("\u00a77Import vanilla Ender Chest data into this plugin:");
                            sender.sendMessage("  \u00a7d/ec migrate vanilla \u00a78- \u00a7aImports all player vanilla chests (online + offline).");
                            sender.sendMessage("");
                            sender.sendMessage("\u00a77Export plugin data back to vanilla Ender Chests:");
                            sender.sendMessage("  \u00a7d/ec export \u00a78- \u00a7cWrites plugin data back into vanilla .dat files.");
                            sender.sendMessage("  \u00a78(Items beyond slot 27 are dropped for online players or deleted for offline players.)");
                            sender.sendMessage("");
                            sender.sendMessage("\u00a77Move data between storage backends:");
                            sender.sendMessage("  \u00a7d/ec migrate to-db \u00a78- \u00a77Move file data \u2192 MySQL.");
                            sender.sendMessage("  \u00a7d/ec migrate to-file \u00a78- \u00a77Move MySQL data \u2192 files.");
                            sender.sendMessage("  \u00a7d/ec migrate from-vec \u00a78- \u00a77Import from VariableEnderChests.");
                            sender.sendMessage("  \u00a7d/ec migrate quickec \u00a78- \u00a77Import from QuickEC (uses the vanilla chest).");
                            sender.sendMessage("  \u00a7d/ec migrate backups \u00a78- \u00a77Migrate old backups to v2 format (with page support).");
                        }
                        sender.sendMessage("\u00a75\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
                        return true;
                    }
                    if (sub2.equals("search")) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(this.getMessage("only-players"));
                            return true;
                        }
                        Player p2 = (Player)sender;
                        if (!this.canUseVirtualEnderChestCommand(p2)) {
                            return true;
                        }
                        if (args.length < 2) {
                            p2.sendMessage(this.prefixed("&cUsage: /ec search <item_name>"));
                            return true;
                        }
                        String searchTerm = String.join((CharSequence)" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
                        List<String> results = this.searchItemsInEnderChest(p2.getUniqueId(), searchTerm);
                        if (results.isEmpty()) {
                            p2.sendMessage(this.prefixed("&cNo items matching '&f" + searchTerm + "&c' found in your EnderChest."));
                        } else {
                            p2.sendMessage(this.prefixed("&aFound &f" + results.size() + " &aitem(s) matching '&f" + searchTerm + "&a':"));
                            for (String result : results) {
                                p2.sendMessage("  \u00a77- " + result);
                            }
                        }
                        return true;
                    }
                    try {
                        int directPage = Integer.parseInt(sub2);
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(this.getMessage("only-players"));
                            return true;
                        }
                        if (!this.pagesEnabled) {
                            sender.sendMessage(this.prefixed("&cPages are disabled."));
                            return true;
                        }
                        Player p3 = (Player)sender;
                        if (!this.canUseVirtualEnderChestCommand(p3)) {
                            return true;
                        }
                        int maxPages = this.getPlayerMaxPages(p3);
                        if (directPage < 1 || directPage > maxPages) {
                            p3.sendMessage(this.prefixed("&cPage must be between 1 and " + maxPages + "."));
                            return true;
                        }
                        this.openEnderChestLogic(p3, p3.getUniqueId(), p3.getName(), false, directPage);
                        return true;
                    }
                    catch (NumberFormatException directPage) {
                        if (sub2.equals("page")) {
                            int pageNum;
                            if (!(sender instanceof Player)) {
                                sender.sendMessage(this.getMessage("only-players"));
                                return true;
                            }
                            if (!this.pagesEnabled) {
                                sender.sendMessage(this.prefixed("&cPages are disabled."));
                                return true;
                            }
                            Player p4 = (Player)sender;
                            if (!this.canUseVirtualEnderChestCommand(p4)) {
                                return true;
                            }
                            if (args.length != 2) {
                                p4.sendMessage(this.prefixed("&cUsage: /ec page <number>"));
                                return true;
                            }
                            try {
                                pageNum = Integer.parseInt(args[1]);
                            }
                            catch (NumberFormatException e) {
                                p4.sendMessage(this.prefixed("&cInvalid page number."));
                                return true;
                            }
                            int maxPages = this.getPlayerMaxPages(p4);
                            if (pageNum < 1 || pageNum > maxPages) {
                                p4.sendMessage(this.prefixed("&cPage must be between 1 and " + maxPages + "."));
                                return true;
                            }
                            this.openEnderChestLogic(p4, p4.getUniqueId(), p4.getName(), false, pageNum);
                            return true;
                        }
                        if (sub2.equals("setrows")) {
                            int rows;
                            if (!sender.hasPermission("ec.admin")) {
                                sender.sendMessage(this.getMessage("no-permission"));
                                return true;
                            }
                            if (args.length != 3) {
                                sender.sendMessage(this.prefixed("&cUsage: /ec setrows <player> <rows (3-6)>"));
                                return true;
                            }
                            Player target = Bukkit.getPlayer((String)args[1]);
                            if (target == null) {
                                sender.sendMessage(this.getMessage("player-not-found"));
                                return true;
                            }
                            try {
                                rows = Integer.parseInt(args[2]);
                            }
                            catch (NumberFormatException e) {
                                sender.sendMessage(this.prefixed("&cRows must be a number between 3 and 6."));
                                return true;
                            }
                            rows = Math.max(3, Math.min(6, rows));
                            this.playerAssignedRows.put(target.getUniqueId(), rows);
                            this.saveAssignedRows(target.getUniqueId(), rows);
                            Inventory activeInventory = this.activeInventories.get(target.getUniqueId());
                            if (activeInventory != null) {
                                for (HumanEntity human : new ArrayList<HumanEntity>(activeInventory.getViewers())) {
                                    if (!(human instanceof Player)) continue;
                                    Player openViewer = (Player)human;
                                    openViewer.closeInventory();
                                    openViewer.sendMessage(this.prefixed("&eThis EnderChest was closed to apply the new size."));
                                }
                            }
                            sender.sendMessage(this.prefixed("&aSet &f" + target.getName() + "'s &aEnderChest to &f" + rows + " rows&a."));
                            target.sendMessage(this.prefixed("&aYour EnderChest size has been set to &f" + rows + " rows &aby an admin."));
                            return true;
                        }
                        if (sub2.equals("resetrows")) {
                            if (!sender.hasPermission("ec.admin")) {
                                sender.sendMessage(this.getMessage("no-permission"));
                                return true;
                            }
                            if (args.length != 2) {
                                sender.sendMessage(this.prefixed("&cUsage: /ec resetrows <player>"));
                                return true;
                            }
                            Player target = Bukkit.getPlayer((String)args[1]);
                            if (target == null) {
                                sender.sendMessage(this.getMessage("player-not-found"));
                                return true;
                            }
                            this.removeAssignedRows(target.getUniqueId());
                            Inventory activeInventory = this.activeInventories.get(target.getUniqueId());
                            if (activeInventory != null) {
                                for (HumanEntity human : new ArrayList<HumanEntity>(activeInventory.getViewers())) {
                                    if (!(human instanceof Player)) continue;
                                    Player openViewer = (Player)human;
                                    openViewer.closeInventory();
                                    openViewer.sendMessage(this.prefixed("&eThis EnderChest was closed to apply the new size."));
                                }
                            }
                            sender.sendMessage(this.prefixed("&aReset &f" + target.getName() + "'s &aEnderChest to default (&f" + this.adminAssignedDefaultRows + " rows&a)."));
                            target.sendMessage(this.prefixed("&aYour EnderChest size has been reset to default (&f" + this.adminAssignedDefaultRows + " rows&a)."));
                            return true;
                        }
                        if (sub2.equals("rowinfo")) {
                            if (!sender.hasPermission("ec.admin")) {
                                sender.sendMessage(this.getMessage("no-permission"));
                                return true;
                            }
                            if (args.length == 1) {
                                sender.sendMessage("\u00a75\u00a7l\u2501\u2501\u2501 Admin-Assigned EnderChest Rows \u2501\u2501\u2501");
                                if (this.playerAssignedRows.isEmpty()) {
                                    sender.sendMessage("\u00a77No custom row assignments.");
                                    sender.sendMessage("\u00a77Default rows: \u00a7f" + this.adminAssignedDefaultRows);
                                } else {
                                    sender.sendMessage("\u00a77Default rows: \u00a7f" + this.adminAssignedDefaultRows);
                                    sender.sendMessage("\u00a77Custom assignments:");
                                    for (Map.Entry<UUID, Integer> entry : this.playerAssignedRows.entrySet()) {
                                        OfflinePlayer op = Bukkit.getOfflinePlayer((UUID)entry.getKey());
                                        String name = op.getName() != null ? op.getName() : entry.getKey().toString().substring(0, 8);
                                        sender.sendMessage("  \u00a7f" + name + " \u00a77\u2192 \u00a7f" + String.valueOf(entry.getValue()) + " rows");
                                    }
                                }
                                return true;
                            }
                            if (args.length == 2) {
                                Player target = Bukkit.getPlayer((String)args[1]);
                                if (target != null) {
                                    this.sendRowInfo(sender, target.getUniqueId(), target.getName());
                                    return true;
                                }
                                String lookupName = args[1];
                                this.resolveOfflinePlayerByName(sender, lookupName, off -> this.sendRowInfo(sender, off.getUniqueId(), off.getName() != null ? off.getName() : lookupName));
                                return true;
                            }
                            sender.sendMessage(this.prefixed("&cUsage: /ec rowinfo [player]"));
                            return true;
                        }
                        if (sub2.equals("setpages")) {
                            int pages;
                            if (!sender.hasPermission("ec.admin")) {
                                sender.sendMessage(this.getMessage("no-permission"));
                                return true;
                            }
                            if (args.length != 3) {
                                sender.sendMessage(this.prefixed("&cUsage: /ec setpages <player> <pages>"));
                                return true;
                            }
                            Player target = Bukkit.getPlayer((String)args[1]);
                            if (target == null) {
                                sender.sendMessage(this.getMessage("player-not-found"));
                                return true;
                            }
                            try {
                                pages = Integer.parseInt(args[2]);
                            }
                            catch (NumberFormatException e) {
                                sender.sendMessage(this.prefixed("&cPages must be a number between 1 and " + this.adminAssignedMaxPages + "."));
                                return true;
                            }
                            pages = Math.max(1, Math.min(this.adminAssignedMaxPages, pages));
                            this.playerAssignedPages.put(target.getUniqueId(), pages);
                            this.saveAssignedPages(target.getUniqueId(), pages);
                            if (this.viewerToOwnerMap.containsKey(target.getUniqueId())) {
                                target.closeInventory();
                                target.sendMessage(this.prefixed("&eYour EnderChest has been closed to apply the page changes."));
                            }
                            sender.sendMessage(this.prefixed("&aSet &f" + target.getName() + "'s &aEnderChest to &f" + pages + " pages&a."));
                            target.sendMessage(this.prefixed("&aYour EnderChest now has &f" + pages + " pages &aby an admin."));
                            return true;
                        }
                        if (sub2.equals("resetpages")) {
                            if (!sender.hasPermission("ec.admin")) {
                                sender.sendMessage(this.getMessage("no-permission"));
                                return true;
                            }
                            if (args.length != 2) {
                                sender.sendMessage(this.prefixed("&cUsage: /ec resetpages <player>"));
                                return true;
                            }
                            Player target = Bukkit.getPlayer((String)args[1]);
                            if (target == null) {
                                sender.sendMessage(this.getMessage("player-not-found"));
                                return true;
                            }
                            this.removeAssignedPages(target.getUniqueId());
                            if (this.viewerToOwnerMap.containsKey(target.getUniqueId())) {
                                target.closeInventory();
                                target.sendMessage(this.prefixed("&eYour EnderChest has been closed to apply the page changes."));
                            }
                            sender.sendMessage(this.prefixed("&aReset &f" + target.getName() + "'s &apages to default (&f" + this.adminAssignedDefaultPages + "&a)."));
                            target.sendMessage(this.prefixed("&aYour EnderChest pages have been reset to default (&f" + this.adminAssignedDefaultPages + "&a)."));
                            return true;
                        }
                        if (sub2.equals("pageinfo")) {
                            if (!sender.hasPermission("ec.admin")) {
                                sender.sendMessage(this.getMessage("no-permission"));
                                return true;
                            }
                            if (args.length == 1) {
                                sender.sendMessage("\u00a75\u00a7l\u2501\u2501\u2501 Admin-Assigned EnderChest Pages \u2501\u2501\u2501");
                                if (this.playerAssignedPages.isEmpty()) {
                                    sender.sendMessage("\u00a77No custom page assignments.");
                                    sender.sendMessage("\u00a77Default pages: \u00a7f" + this.adminAssignedDefaultPages);
                                } else {
                                    sender.sendMessage("\u00a77Default pages: \u00a7f" + this.adminAssignedDefaultPages);
                                    sender.sendMessage("\u00a77Custom assignments:");
                                    for (Map.Entry<UUID, Integer> entry : this.playerAssignedPages.entrySet()) {
                                        OfflinePlayer op = Bukkit.getOfflinePlayer((UUID)entry.getKey());
                                        String name = op.getName() != null ? op.getName() : entry.getKey().toString().substring(0, 8);
                                        sender.sendMessage("  \u00a7f" + name + " \u00a77\u2192 \u00a7f" + String.valueOf(entry.getValue()) + " pages");
                                    }
                                }
                                return true;
                            }
                            if (args.length == 2) {
                                Player target = Bukkit.getPlayer((String)args[1]);
                                if (target != null) {
                                    this.sendPageInfo(sender, target.getUniqueId(), target.getName());
                                    return true;
                                }
                                String lookupName = args[1];
                                this.resolveOfflinePlayerByName(sender, lookupName, off -> this.sendPageInfo(sender, off.getUniqueId(), off.getName() != null ? off.getName() : lookupName));
                                return true;
                            }
                            sender.sendMessage(this.prefixed("&cUsage: /ec pageinfo [player]"));
                            return true;
                        }
                        if (!sub2.equals("reload") && !sub2.equals("migrate") && !sub2.equals("export") && !sub2.equals("backup") && !sub2.equals("autobackup") && !sub2.equals("backupannounce") && !sub2.equals("clear") && !sub2.equals("clearall") && !sub2.equals("repair")) break block160;
                        if (!sender.hasPermission("ec.admin")) {
                            sender.sendMessage(this.getMessage("no-permission"));
                            return true;
                        }
                        if (sub2.equals("reload")) {
                            this.closeAllInventoriesAsync();
                            this.loadConfigValues();
                            sender.sendMessage(this.getMessage("reload-success"));
                            return true;
                        }
                        if (sub2.equals("migrate")) {
                            if (args.length != 2) {
                                sender.sendMessage(this.getMessage("prefix") + "\u00a7cUsage: /ec migrate <to-db|to-file|from-vec|vanilla|quickec|backups>");
                                return true;
                            }
                            if (args[1].equalsIgnoreCase("backups")) {
                                if (this.backupManager != null) {
                                    this.backupManager.migrateOldBackups(sender);
                                } else {
                                    sender.sendMessage(this.prefixed("&cBackup system is not ready."));
                                }
                                return true;
                            }
                            this.runMigration(args[1], sender);
                            return true;
                        }
                        if (sub2.equals("export")) {
                            this.runExport(sender);
                            return true;
                        }
                        if (sub2.equals("backup")) {
                            if (!this.getConfig().getBoolean("backups.enabled", true)) {
                                sender.sendMessage(this.prefixed("&cThe backup system is disabled in the config."));
                                return true;
                            }
                            if (args.length == 1) {
                                if (sender instanceof Player) {
                                    this.openBackupMainGui((Player)sender);
                                } else {
                                    sender.sendMessage(this.prefixed("&cUsage: /ec backup <create|restore|delete>"));
                                }
                                return true;
                            }
                            String action = args[1].toLowerCase();
                            if (action.equals("create")) {
                                if (args.length < 3) {
                                    sender.sendMessage(this.prefixed("&cUsage: /ec backup create <name>"));
                                    return true;
                                }
                                this.backupManager.createBackupAsync(String.join((CharSequence)" ", Arrays.copyOfRange(args, 2, args.length)), sender);
                                return true;
                            }
                            if (action.equals("restore")) {
                                BackupManager.RestoreMode restoreMode;
                                if (args.length < 4) {
                                    sender.sendMessage(this.prefixed("&cUsage: /ec backup restore <name> <kick|live>"));
                                    return true;
                                }
                                if (args[3].equalsIgnoreCase("kick")) {
                                    restoreMode = BackupManager.RestoreMode.KICK_PLAYERS;
                                } else if (args[3].equalsIgnoreCase("live") || args[3].equalsIgnoreCase("freeze")) {
                                    restoreMode = BackupManager.RestoreMode.LIVE_FREEZE;
                                } else {
                                    sender.sendMessage(this.prefixed("&cRestore mode must be kick or live."));
                                    return true;
                                }
                                this.backupManager.restoreBackupAsync(args[2], restoreMode, sender);
                                return true;
                            }
                            if (action.equals("delete")) {
                                if (args.length < 3) {
                                    sender.sendMessage(this.prefixed("&cUsage: /ec backup delete <name>"));
                                    return true;
                                }
                                if (this.backupManager.deleteBackup(args[2])) {
                                    sender.sendMessage(this.prefixed("&aBackup '&f" + args[2] + "&a' was deleted."));
                                } else {
                                    sender.sendMessage(this.prefixed("&cBackup '&f" + args[2] + "&c' was not found."));
                                }
                                return true;
                            }
                            if (sender instanceof Player) {
                                this.openBackupMainGui((Player)sender);
                            } else {
                                sender.sendMessage(this.prefixed("&cUsage: /ec backup <create|restore|delete>"));
                            }
                            return true;
                        }
                        if (sub2.equals("autobackup")) {
                            boolean enabled;
                            if (args.length < 2) {
                                sender.sendMessage(this.prefixed("&cUsage: /ec autobackup <on|off> [HH:mm]"));
                                return true;
                            }
                            if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("enable")) {
                                enabled = true;
                            } else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("disable")) {
                                enabled = false;
                            } else {
                                sender.sendMessage(this.prefixed("&cUsage: /ec autobackup <on|off> [HH:mm]"));
                                return true;
                            }
                            String time = args.length >= 3 ? args[2] : this.backupManager.getAutoBackupTime();
                            this.backupManager.setAutoBackupSettings(enabled, time);
                            sender.sendMessage(this.prefixed("&aAuto backups are now " + (String)(enabled ? "enabled at &f" + this.backupManager.getAutoBackupTime() : "disabled") + "&a."));
                            return true;
                        }
                        if (sub2.equals("backupannounce")) {
                            boolean enabled;
                            if (args.length != 2) {
                                sender.sendMessage(this.prefixed("&cUsage: /ec backupannounce <on|off>"));
                                return true;
                            }
                            if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("enable")) {
                                enabled = true;
                            } else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("disable")) {
                                enabled = false;
                            } else {
                                sender.sendMessage(this.prefixed("&cUsage: /ec backupannounce <on|off>"));
                                return true;
                            }
                            this.getConfig().set("backups.chat-announcements", (Object)enabled);
                            this.saveConfig();
                            sender.sendMessage(this.prefixed("&aBackup chat announcements are now " + (enabled ? "enabled" : "disabled") + "&a."));
                            return true;
                        }
                        if (sub2.equals("clear")) {
                            if (args.length != 2 && args.length != 3) {
                                sender.sendMessage(this.prefixed("&cUsage: /ec clear <player> [page]"));
                                return true;
                            }
                            String targetName = args[1];
                            Player target = Bukkit.getPlayer((String)targetName);
                            if (target != null) {
                                this.handleClearTarget(sender, target.getUniqueId(), target.getName(), args);
                                return true;
                            }
                            this.resolveOfflinePlayerByName(sender, targetName, off -> {
                                boolean known;
                                boolean bl = known = off.hasPlayedBefore() || this.namesConfig.contains(off.getUniqueId().toString());
                                if (!known) {
                                    sender.sendMessage(this.getMessage("player-never-played").replace("{player}", targetName));
                                    return;
                                }
                                this.handleClearTarget(sender, off.getUniqueId(), off.getName() != null ? off.getName() : targetName, args);
                            });
                            return true;
                        }
                        if (sub2.equals("repair")) {
                            this.repairManager.handleCommand(sender, args);
                            return true;
                        }
                        if (!sub2.equals("clearall")) break block160;
                        if (args.length == 1) {
                            sender.sendMessage(this.prefixed("&cAre you sure you want to clear ALL player EnderChests?"));
                            sender.sendMessage(this.prefixed("&cThis action cannot be undone. Type &d/ec clearall confirm &cto proceed."));
                            return true;
                        }
                        if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
                            this.clearAllPlayerData();
                            sender.sendMessage(this.prefixed("&aSuccessfully cleared ALL EnderChests for all players."));
                            return true;
                        }
                        sender.sendMessage(this.prefixed("&cUsage: /ec clearall [confirm]"));
                        return true;
                    }
                }
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(this.getMessage("only-players"));
                return true;
            }
            p = (Player)sender;
            if (args.length == 0) {
                if (!this.canUseVirtualEnderChestCommand(p)) {
                    return true;
                }
                this.openEnderChestLogic(p, p.getUniqueId(), p.getName(), false);
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                return true;
            }
            sub = args[0].toLowerCase();
            if (sub.equals("invite")) {
                Player target;
                String targetName;
                if (!(args.length == 2 || this.pagesEnabled && args.length == 3)) {
                    p.sendMessage(this.getMessage("invite-usage"));
                    return true;
                }
                boolean inviteAllPages = false;
                int invitePage = 1;
                if (this.pagesEnabled && args.length == 3) {
                    String scopeArg = args[1].toLowerCase(Locale.ROOT);
                    targetName = args[2];
                    if (INVITE_SCOPE_ALL.equals(scopeArg)) {
                        inviteAllPages = true;
                    } else {
                        try {
                            invitePage = Integer.parseInt(scopeArg);
                        }
                        catch (NumberFormatException e) {
                            p.sendMessage(this.getMessage("invite-usage"));
                            return true;
                        }
                        int playerMaxPages = this.getPlayerMaxPages(p);
                        if (invitePage < 1 || invitePage > playerMaxPages) {
                            p.sendMessage(this.prefixed("&cPage must be between 1 and " + playerMaxPages + "."));
                            return true;
                        }
                    }
                } else {
                    targetName = args[1];
                }
                if ((target = Bukkit.getPlayer((String)targetName)) == null) {
                    p.sendMessage(this.getMessage("player-not-found"));
                    return true;
                }
                if (target.equals((Object)p)) {
                    p.sendMessage(this.getMessage("self-interact"));
                    return true;
                }
                boolean changed = this.inviteService.save(p.getUniqueId(), target.getUniqueId(), invitePage, inviteAllPages);
                if (!changed) {
                    p.sendMessage(this.getMessage("invite-already"));
                } else {
                    Object scopeText = "";
                    if (this.pagesEnabled) {
                        scopeText = inviteAllPages ? " \u00a77(all pages)" : " \u00a77(page " + invitePage + ")";
                    }
                    p.sendMessage(this.getMessage("invite-added").replace("{player}", target.getName()) + (String)scopeText);
                    target.sendMessage(this.getMessage("prefix") + "\u00a7aYou have been invited to " + p.getName() + "'s EnderChest" + (String)scopeText + "\u00a7a.");
                }
                return true;
            }
            if (sub.equals("uninvite")) {
                if (args.length != 2) {
                    p.sendMessage(this.getMessage("invite-usage"));
                    return true;
                }
                String targetName = args[1];
                Player onlineTarget = Bukkit.getPlayer((String)targetName);
                if (onlineTarget != null) {
                    this.handleUninviteTarget(p, onlineTarget.getUniqueId(), targetName);
                    return true;
                }
                Player invitingPlayer = p;
                this.resolveOfflinePlayerByName((CommandSender)invitingPlayer, targetName, off -> this.handleUninviteTarget(invitingPlayer, off.getUniqueId(), targetName));
                return true;
            }
            if (sub.equals("invitelist")) {
                List<String> names = this.inviteService.descriptions(p.getUniqueId());
                String list = names.isEmpty() ? "\u00a7cNone" : String.join((CharSequence)", ", names);
                p.sendMessage(this.getMessage("invite-list").replace("{list}", list));
                return true;
            }
            p.sendMessage("\u00a77Unknown subcommand. Use \u00a7d/ec help \u00a77for a list of commands.");
            return true;
        }
        if (command.getName().equalsIgnoreCase("ecsee")) {
            return this.ecSeeCommand.execute(sender, args);
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("ecshare")) {
            if (args.length == 1) {
                return null;
            }
            if (args.length == 2 && this.pagesEnabled) {
                ArrayList<String> pages = new ArrayList<String>();
                for (int i = 1; i <= this.maxPages; ++i) {
                    pages.add(String.valueOf(i));
                }
                return pages;
            }
        }
        if (command.getName().equalsIgnoreCase("ecsee")) {
            if (args.length == 1) {
                return null;
            }
            if (args.length == 2 && this.pagesEnabled && sender.hasPermission("ec.see")) {
                ArrayList<String> pages = new ArrayList<String>();
                for (int i = 1; i <= this.maxPages; ++i) {
                    pages.add(String.valueOf(i));
                }
                return pages;
            }
        }
        if (command.getName().equalsIgnoreCase("ec")) {
            if (args.length == 1) {
                ArrayList<String> list = new ArrayList<String>();
                list.add("help");
                list.add("invite");
                list.add("uninvite");
                list.add("invitelist");
                list.add("search");
                if (this.pagesEnabled) {
                    list.add("page");
                }
                if (sender.hasPermission("ec.admin")) {
                    list.add("reload");
                    list.add("migrate");
                    list.add("export");
                    list.add("backup");
                    list.add("autobackup");
                    list.add("backupannounce");
                    list.add("setrows");
                    list.add("resetrows");
                    list.add("rowinfo");
                    list.add("setpages");
                    list.add("resetpages");
                    list.add("pageinfo");
                    list.add("clear");
                    list.add("clearall");
                    list.add("repair");
                }
                return list.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("migrate")) {
                return Arrays.asList("to-db", "to-file", "from-vec", "vanilla", "quickec", "backups");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("backup")) {
                return Arrays.asList("create", "restore", "delete");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("autobackup")) {
                return Arrays.asList("on", "off");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("backupannounce")) {
                return Arrays.asList("on", "off");
            }
            if (args.length == 4 && args[0].equalsIgnoreCase("backup") && args[1].equalsIgnoreCase("restore")) {
                return Arrays.asList("kick", "live");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("setrows")) {
                return null;
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("setrows")) {
                return Arrays.asList("3", "4", "5", "6");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("setpages")) {
                return null;
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("setpages")) {
                ArrayList<String> pages = new ArrayList<String>();
                for (int i = 1; i <= this.adminAssignedMaxPages; ++i) {
                    pages.add(String.valueOf(i));
                }
                return pages;
            }
            if (args.length == 2 && (args[0].equalsIgnoreCase("resetrows") || args[0].equalsIgnoreCase("resetpages") || args[0].equalsIgnoreCase("rowinfo") || args[0].equalsIgnoreCase("pageinfo") || args[0].equalsIgnoreCase("clear"))) {
                return null;
            }
            if (args.length >= 2 && args[0].equalsIgnoreCase("repair")) {
                return sender.hasPermission("ec.admin") ? this.repairManager.tabComplete(sender, args) : List.of();
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("clear") && this.pagesEnabled) {
                ArrayList<String> pages = new ArrayList<String>();
                for (int i = 1; i <= this.maxPages; ++i) {
                    pages.add(String.valueOf(i));
                }
                return pages;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("clearall")) {
                return Arrays.asList("confirm");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("page")) {
                ArrayList<String> pages = new ArrayList<String>();
                if (sender instanceof Player) {
                    int max = this.getPlayerMaxPages((Player)sender);
                    for (int i = 1; i <= max; ++i) {
                        pages.add(String.valueOf(i));
                    }
                } else {
                    for (int i = 1; i <= this.maxPages; ++i) {
                        pages.add(String.valueOf(i));
                    }
                }
                return pages;
            }
            if (args[0].equalsIgnoreCase("invite")) {
                if (args.length == 2 && this.pagesEnabled) {
                    ArrayList<String> suggestions = new ArrayList<String>();
                    suggestions.add(INVITE_SCOPE_ALL);
                    if (sender instanceof Player) {
                        int max = this.getPlayerMaxPages((Player)sender);
                        for (int i = 1; i <= max; ++i) {
                            suggestions.add(String.valueOf(i));
                        }
                    }
                    suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
                    return suggestions.stream().filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
                }
                if (args.length == 2 && !this.pagesEnabled || args.length == 3) {
                    return null;
                }
            }
        }
        return null;
    }

    @EventHandler
    public void onBackupNameChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!this.pendingBackupNameInput.remove(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        String input = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        this.foliaLib.getScheduler().runAtEntity((Entity)player, task -> {
            if (input.equalsIgnoreCase("cancel")) {
                player.sendMessage(this.prefixed("&eBackup creation cancelled."));
                return;
            }
            if (!player.hasPermission("ec.admin")) {
                player.sendMessage(this.getMessage("no-permission"));
                return;
            }
            this.backupManager.createBackupAsync(input, (CommandSender)player);
        });
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ENDER_CHEST) {
            Player p = event.getPlayer();
            if (this.hasVanillaBypass(p)) {
                return;
            }
            event.setCancelled(true);
            if (!this.canOpenPhysicalEnderChest(p)) {
                return;
            }
            this.openEnderChestLogic(p, p.getUniqueId(), p.getName(), false);
        }
    }

    void handleInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem;
        ItemStack cursorItem;
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        if (this.isBackupGui(event.getView().getTitle())) {
            this.handleBackupInventoryClick(event);
            return;
        }
        Inventory top = event.getView().getTopInventory();
        if (this.isEnderChestInventory(top) && !this.hasValidSession(player, top)) {
            this.rejectInventoryUse(player, event, "interacted with an EnderChest window that is no longer valid");
            return;
        }
        UUID ownerId = this.viewerToOwnerMap.get(player.getUniqueId());
        if (ownerId == null) {
            return;
        }
        Inventory inv = this.activeInventories.get(ownerId);
        if (inv == null || !top.equals((Object)inv)) {
            return;
        }
        if (!ownerId.equals(player.getUniqueId()) && !this.isTeamChestMember(player, ownerId)) {
            boolean hasPermission;
            int activePage = this.activeInventoryPage.getOrDefault(ownerId, 1);
            boolean bl = hasPermission = player.hasPermission("ec.see") || this.inviteService.hasAccess(ownerId, player.getUniqueId(), activePage);
            if (!hasPermission) {
                event.setCancelled(true);
                player.closeInventory();
                this.viewerToOwnerMap.remove(player.getUniqueId());
                this.getLogger().warning("[Anti-Exploit] Player " + player.getName() + " tried to access chest without permission!");
                return;
            }
        }
        if ((cursorItem = event.getCursor()) != null && this.isItemBlacklisted(cursorItem) && event.getClickedInventory() == inv) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.blacklistMessage));
            this.playSound(player, "ENTITY_VILLAGER_NO");
            return;
        }
        if (event.getAction() == InventoryAction.HOTBAR_SWAP && event.getClickedInventory() == inv) {
            ItemStack swapItem;
            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                swapItem = player.getInventory().getItemInOffHand();
            } else {
                int hotbarButton = event.getHotbarButton();
                ItemStack itemStack = swapItem = hotbarButton >= 0 && hotbarButton < 9 ? player.getInventory().getItem(hotbarButton) : null;
            }
            if (this.isItemBlacklisted(swapItem)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.blacklistMessage));
                this.playSound(player, "ENTITY_VILLAGER_NO");
                return;
            }
        }
        if (event.isShiftClick() && event.getClickedInventory() != inv && this.isItemBlacklisted(currentItem = event.getCurrentItem())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.blacklistMessage));
            this.playSound(player, "ENTITY_VILLAGER_NO");
        }
    }

    private boolean isEnderChestInventory(Inventory inv) {
        return this.holderOf(inv) != null;
    }

    private EnderChestHolder holderOf(Inventory inv) {
        EnderChestHolder holder;
        if (inv == null) {
            return null;
        }
        InventoryHolder inventoryHolder = inv.getHolder();
        return inventoryHolder instanceof EnderChestHolder ? (holder = (EnderChestHolder)inventoryHolder) : null;
    }

    private long nextGeneration(UUID ownerId) {
        return this.chestGeneration.merge(ownerId, 1L, Long::sum);
    }

    private boolean isCurrentGeneration(EnderChestHolder holder) {
        if (holder == null) {
            return false;
        }
        Long current = this.chestGeneration.get(holder.getOwnerId());
        return current == null || current.longValue() == holder.getGeneration();
    }

    private boolean hasValidSession(Player viewer, Inventory inv) {
        EnderChestHolder holder = this.holderOf(inv);
        if (holder == null || !this.isCurrentGeneration(holder)) {
            return false;
        }
        UUID sessionOwner = this.viewerToOwnerMap.get(viewer.getUniqueId());
        if (sessionOwner == null || !sessionOwner.equals(holder.getOwnerId())) {
            return false;
        }
        Inventory tracked = this.activeInventories.get(holder.getOwnerId());
        return tracked != null && tracked.equals((Object)inv);
    }

    private String ownerNameFor(UUID ownerId) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer((UUID)ownerId);
        return owner.getName() == null ? "Unknown" : owner.getName();
    }

    private void persistWindow(EnderChestHolder holder, ItemStack[] contents) {
        UUID ownerId = holder.getOwnerId();
        int page = holder.getPage();
        long generation = holder.getGeneration();
        boolean paged = holder.isPaged();
        String resolvedName = "Unknown";
        if (holder.isTeam()) {
            resolvedName = holder.getOwnerName();
        } else {
            OfflinePlayer owner = Bukkit.getOfflinePlayer((UUID)ownerId);
            if (owner.getName() != null) {
                resolvedName = owner.getName();
            }
        }
        String nameToSave = resolvedName;
        this.pendingSaves.add(ownerId);
        if (paged) {
            this.activeInventoryPage.remove(ownerId);
        }
        this.foliaLib.getScheduler().runAsync(task -> {
            try {
                Object object = this.getSaveLock(ownerId);
                synchronized (object) {
                    block10: {
                        Long current = this.chestGeneration.get(ownerId);
                        if (current == null || current == generation) break block10;
                        this.getLogger().info("[Anti-Exploit] Dropped a stale save for " + String.valueOf(ownerId) + " (generation " + generation + ", current " + current + ").");
                        return;
                    }
                    if (paged) {
                        this.savePageItems(ownerId, page, contents);
                    } else {
                        this.saveInventorySync(ownerId, nameToSave, contents);
                    }
                }
            }
            finally {
                this.pendingSaves.remove(ownerId);
                this.stopLockTask(ownerId);
                this.unlockChest(ownerId);
            }
        });
    }

    private void rejectInventoryUse(Player viewer, InventoryClickEvent event, String reason) {
        if (event != null) {
            event.setCancelled(true);
        }
        this.viewerToOwnerMap.remove(viewer.getUniqueId());
        this.playerCurrentPage.remove(viewer.getUniqueId());
        this.foliaLib.getScheduler().runAtEntity((Entity)viewer, task -> viewer.closeInventory());
        this.getLogger().warning("[Anti-Exploit] " + viewer.getName() + ": " + reason);
    }

    void handleInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (this.isBackupGui(event.getView().getTitle())) {
            event.setCancelled(true);
            return;
        }
        Player player = (Player)event.getWhoClicked();
        Inventory top = event.getView().getTopInventory();
        if (this.isEnderChestInventory(top) && !this.hasValidSession(player, top)) {
            event.setCancelled(true);
            this.rejectInventoryUse(player, null, "dragged items in an EnderChest window that is no longer valid");
            return;
        }
        UUID ownerId = this.viewerToOwnerMap.get(player.getUniqueId());
        if (ownerId == null) {
            return;
        }
        Inventory inv = this.activeInventories.get(ownerId);
        if (inv != null && event.getInventory().equals((Object)inv)) {
            if (!ownerId.equals(player.getUniqueId()) && !this.isTeamChestMember(player, ownerId)) {
                boolean hasPermission;
                int activePage = this.activeInventoryPage.getOrDefault(ownerId, 1);
                boolean bl = hasPermission = player.hasPermission("ec.see") || this.inviteService.hasAccess(ownerId, player.getUniqueId(), activePage);
                if (!hasPermission) {
                    event.setCancelled(true);
                    this.getLogger().warning("[Anti-Exploit] Player " + player.getName() + " drag attempt without permission!");
                    return;
                }
            }
            for (ItemStack item : event.getNewItems().values()) {
                if (!this.isItemBlacklisted(item)) continue;
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)this.blacklistMessage));
                this.playSound(player, "ENTITY_VILLAGER_NO");
                return;
            }
        }
    }

    void handleInventoryClose(InventoryCloseEvent event) {
        Player viewer = (Player)event.getPlayer();
        if (BACKUP_MODE_TITLE.equals(event.getView().getTitle())) {
            this.selectedBackupRestore.remove(viewer.getUniqueId());
        }
        UUID viewerId = viewer.getUniqueId();
        UUID ownerId = this.viewerToOwnerMap.get(viewerId);
        EnderChestHolder closedHolder = this.holderOf(event.getInventory());
        if (ownerId == null || this.activeInventories.get(ownerId) == null || !event.getInventory().equals((Object)this.activeInventories.get(ownerId))) {
            if (closedHolder != null && !this.switchingPages.contains(viewerId)) {
                if (this.isCurrentGeneration(closedHolder)) {
                    this.getLogger().warning("[Anti-Exploit] " + viewer.getName() + " closed an untracked EnderChest window - saving its contents before dropping it.");
                    this.persistWindow(closedHolder, event.getInventory().getContents());
                } else {
                    this.getLogger().info("[Anti-Exploit] Ignored a superseded EnderChest window closed by " + viewer.getName() + " (generation " + closedHolder.getGeneration() + ").");
                }
                this.activeInventories.remove(closedHolder.getOwnerId(), event.getInventory());
            }
            this.viewerToOwnerMap.remove(viewerId);
            this.playerCurrentPage.remove(viewerId);
            return;
        }
        Inventory inv = this.activeInventories.get(ownerId);
        this.viewerToOwnerMap.remove(viewerId);
        this.playerCurrentPage.remove(viewerId);
        if (inv.getViewers().size() <= 1) {
            this.activeInventories.remove(ownerId);
            if (closedHolder == null) {
                this.getLogger().warning("[EnderChest] Tracked inventory for " + String.valueOf(ownerId) + " had no holder - saving it from the session state.");
                Integer sessionPage = this.activeInventoryPage.get(ownerId);
                closedHolder = new EnderChestHolder(ownerId, viewer.getName(), sessionPage == null ? 1 : sessionPage, this.chestGeneration.getOrDefault(ownerId, 0L), false, this.pagesEnabled && sessionPage != null);
            }
            this.persistWindow(closedHolder, inv.getContents());
            if (this.showCloseMessage && !closedHolder.isTeam()) {
                viewer.sendMessage(this.getMessage("chest-saved"));
            }
        }
        this.playSound(viewer, this.closeSound);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.handleDisconnect(event.getPlayer());
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerKick(PlayerKickEvent event) {
        this.handleDisconnect(event.getPlayer());
    }

    private void handleDisconnect(Player player) {
        this.getChestRows(player, false);
        if (this.pagesEnabled) {
            this.getPlayerMaxPages(player);
        }
        UUID viewerId = player.getUniqueId();
        UUID ownerId = this.viewerToOwnerMap.remove(viewerId);
        this.playerCurrentPage.remove(viewerId);
        this.pendingViewerOpens.remove(viewerId);
        this.switchingPages.remove(viewerId);
        this.selectedBackupRestore.remove(viewerId);
        this.pendingBackupNameInput.remove(viewerId);
        if (ownerId != null) {
            Inventory inv = this.activeInventories.get(ownerId);
            EnderChestHolder holder = this.holderOf(inv);
            if (inv != null && holder != null && inv.getViewers().size() <= 1) {
                this.activeInventories.remove(ownerId);
                this.persistWindow(holder, inv.getContents());
            }
        }
    }

    private void closeAllInventories() {
        for (UUID ownerId : new ArrayList<UUID>(this.activeInventories.keySet())) {
            Inventory inv = this.activeInventories.get(ownerId);
            if (inv == null) {
                this.activeInventoryPage.remove(ownerId);
                this.stopLockTask(ownerId);
                this.unlockChest(ownerId);
                continue;
            }
            Integer activePage = this.activeInventoryPage.remove(ownerId);
            if (activePage != null) {
                this.savePageItems(ownerId, activePage, inv.getContents());
            } else {
                this.saveInventorySync(ownerId, "Unknown", inv.getContents());
            }
            for (HumanEntity viewer : new ArrayList<HumanEntity>(inv.getViewers())) {
                if (viewer instanceof Player) {
                    Player player = (Player)viewer;
                    this.viewerToOwnerMap.remove(player.getUniqueId());
                    this.playerCurrentPage.remove(player.getUniqueId());
                }
                viewer.closeInventory();
                viewer.sendMessage(String.valueOf(ChatColor.YELLOW) + "Server Reload/Restart.");
            }
            this.stopLockTask(ownerId);
            this.unlockChest(ownerId);
        }
        this.activeInventories.clear();
        this.viewerToOwnerMap.clear();
        this.pendingViewerOpens.clear();
    }

    private void closeAllInventoriesAsync() {
        for (UUID ownerId : new ArrayList<UUID>(this.activeInventories.keySet())) {
            Inventory inv = this.activeInventories.remove(ownerId);
            if (inv == null) {
                this.activeInventoryPage.remove(ownerId);
                this.stopLockTask(ownerId);
                this.unlockChest(ownerId);
                continue;
            }
            Integer activePage = this.activeInventoryPage.remove(ownerId);
            ItemStack[] itemsToSave = inv.getContents();
            UUID finalOwnerId = ownerId;
            this.pendingSaves.add(ownerId);
            this.foliaLib.getScheduler().runAsync(task -> {
                try {
                    Object object = this.getSaveLock(finalOwnerId);
                    synchronized (object) {
                        if (activePage != null) {
                            this.savePageItems(finalOwnerId, activePage, itemsToSave);
                        } else {
                            this.saveInventorySync(finalOwnerId, "Unknown", itemsToSave);
                        }
                    }
                }
                finally {
                    this.pendingSaves.remove(finalOwnerId);
                    this.stopLockTask(finalOwnerId);
                    this.unlockChest(finalOwnerId);
                }
            });
            for (HumanEntity viewer : new ArrayList<HumanEntity>(inv.getViewers())) {
                if (viewer instanceof Player) {
                    Player player = (Player)viewer;
                    this.viewerToOwnerMap.remove(player.getUniqueId());
                    this.playerCurrentPage.remove(player.getUniqueId());
                }
                viewer.closeInventory();
                viewer.sendMessage(String.valueOf(ChatColor.YELLOW) + "Server Reload/Restart.");
            }
        }
        this.viewerToOwnerMap.clear();
        this.pendingViewerOpens.clear();
    }

    private void playSound(Player p, String soundName) {
        try {
            Sound s = null;
            try {
                s = (Sound)Sound.class.getMethod("valueOf", String.class).invoke(null, soundName.toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (s != null) {
                p.playSound(p.getLocation(), s, 1.0f, 1.0f);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private boolean reopenExistingInventory(Player viewer, UUID ownerId) {
        Inventory existingInv = this.activeInventories.get(ownerId);
        if (existingInv == null) {
            this.activeInventories.remove(ownerId);
            return false;
        }
        if (existingInv.equals((Object)viewer.getOpenInventory().getTopInventory())) {
            this.viewerToOwnerMap.put(viewer.getUniqueId(), ownerId);
            return true;
        }
        viewer.closeInventory();
        this.activeInventories.put(ownerId, existingInv);
        if (!this.safeOpenInventory(viewer, existingInv)) {
            this.activeInventories.remove(ownerId);
            this.stopLockTask(ownerId);
            this.unlockChest(ownerId);
            return false;
        }
        this.viewerToOwnerMap.put(viewer.getUniqueId(), ownerId);
        this.playSound(viewer, this.openSound);
        return true;
    }

    private void saveCurrentPageAndSwitch(Player player, UUID ownerId, int currentPage, int newPage) {
        Inventory inv = this.activeInventories.get(ownerId);
        if (inv == null) {
            return;
        }
        int pageToSave = this.activeInventoryPage.getOrDefault(ownerId, currentPage);
        if (pageToSave == newPage) {
            player.sendMessage(this.getMessage("already-viewing"));
            return;
        }
        UUID viewerId = player.getUniqueId();
        this.switchingPages.add(viewerId);
        this.pendingSaves.add(ownerId);
        this.activeInventories.remove(ownerId);
        this.activeInventoryPage.remove(ownerId);
        for (HumanEntity human : new ArrayList<HumanEntity>(inv.getViewers())) {
            if (human instanceof Player) {
                Player other = (Player)human;
                UUID otherId = other.getUniqueId();
                this.viewerToOwnerMap.remove(otherId);
                this.playerCurrentPage.remove(otherId);
                if (!otherId.equals(viewerId)) {
                    other.sendMessage(this.prefixed("&eThis EnderChest was switched to another page."));
                }
            }
            human.closeInventory();
        }
        ItemStack[] allItems = inv.getContents();
        this.foliaLib.getScheduler().runAsync(task -> {
            ItemStack[] newPageItems = null;
            boolean loadFailed = false;
            try {
                Object object = this.getSaveLock(ownerId);
                synchronized (object) {
                    this.savePageItems(ownerId, pageToSave, allItems);
                }
                newPageItems = this.loadPageItems(ownerId, newPage);
                loadFailed = this.loadFailures.remove(ownerId);
            }
            catch (Exception e) {
                this.getLogger().warning("Failed to switch EnderChest page for " + player.getName() + ": " + e.getMessage());
            }
            finally {
                this.pendingSaves.remove(ownerId);
            }
            ItemStack[] finalNewPageItems = newPageItems;
            boolean finalLoadFailed = loadFailed;
            this.foliaLib.getScheduler().runAtEntity((Entity)player, task2 -> {
                try {
                    if (finalNewPageItems == null || finalLoadFailed) {
                        this.stopLockTask(ownerId);
                        this.unlockChest(ownerId);
                        if (player.isOnline()) {
                            player.sendMessage(this.prefixed((String)(finalLoadFailed ? "&cPage " + newPage + " could not be loaded, so it was not opened. Your items are still stored - ask an admin to check the console." : "&cYour EnderChest page could not be switched. Please try again.")));
                        }
                        return;
                    }
                    if (!player.isOnline()) {
                        this.stopLockTask(ownerId);
                        this.unlockChest(ownerId);
                        return;
                    }
                    String title = this.getPagedInventoryTitle(newPage, this.getPlayerMaxPages(ownerId));
                    int pageRows = Math.max(3, Math.min(6, this.computeOwnerChestRows(ownerId)));
                    int storageSize = pageRows * 9;
                    EnderChestHolder holder = new EnderChestHolder(ownerId, player.getName(), newPage, this.nextGeneration(ownerId), false, true);
                    Inventory newInv = Bukkit.createInventory((InventoryHolder)holder, (int)storageSize, (String)title);
                    holder.setInventory(newInv);
                    ArrayList<ItemStack> overflowItems = new ArrayList<ItemStack>();
                    for (int i = 0; i < finalNewPageItems.length; ++i) {
                        if (i < storageSize) {
                            newInv.setItem(i, finalNewPageItems[i]);
                            continue;
                        }
                        if (finalNewPageItems[i] == null || finalNewPageItems[i].getType() == Material.AIR) continue;
                        overflowItems.add(finalNewPageItems[i]);
                    }
                    this.activeInventories.put(ownerId, newInv);
                    this.activeInventoryPage.put(ownerId, newPage);
                    if (!this.safeOpenInventory(player, newInv)) {
                        this.activeInventories.remove(ownerId);
                        this.activeInventoryPage.remove(ownerId);
                        this.stopLockTask(ownerId);
                        this.unlockChest(ownerId);
                        return;
                    }
                    this.viewerToOwnerMap.put(viewerId, ownerId);
                    this.playerCurrentPage.put(viewerId, newPage);
                    this.playSound(player, this.openSound);
                    if (!overflowItems.isEmpty()) {
                        for (ItemStack overflow : overflowItems) {
                            player.getWorld().dropItem(player.getLocation(), overflow);
                        }
                        player.sendMessage(this.getMessage("size-reduced-online").replace("{count}", String.valueOf(overflowItems.size())));
                    }
                }
                finally {
                    this.switchingPages.remove(viewerId);
                }
            });
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void savePageItems(UUID ownerId, int page, ItemStack[] items) {
        Object object = this.getSaveLock(ownerId);
        synchronized (object) {
            if (this.storageType.equalsIgnoreCase("mysql")) {
                try (Connection conn = this.getSQLConnection();
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO " + this.table + " (uuid, data, name) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE data=?, name=?");){
                    String b64 = EnderChest.itemStackArrayToBase64(items);
                    String pageUUID = this.getPageStorageKey(ownerId, page);
                    ps.setString(1, pageUUID);
                    ps.setString(2, b64);
                    ps.setString(3, "Page" + page);
                    ps.setString(4, b64);
                    ps.setString(5, "Page" + page);
                    ps.executeUpdate();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                File file = new File(this.dataFolder, this.getPageStorageKey(ownerId, page) + ".data");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));){
                    writer.write(EnderChest.itemStackArrayToBase64(items));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private ItemStack[] loadPageItems(UUID ownerId, int page) {
        if (this.storageType.equalsIgnoreCase("mysql")) {
            try (Connection conn = this.getSQLConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT data FROM " + this.table + " WHERE uuid=?");){
                String pageUUID = this.getPageStorageKey(ownerId, page);
                ps.setString(1, pageUUID);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return new ItemStack[0];
                ItemStack[] itemStackArray = EnderChest.itemStackArrayFromBase64(rs.getString("data"));
                return itemStackArray;
            }
            catch (Exception e) {
                this.loadFailures.add(ownerId);
                this.getLogger().warning("[EnderChest] Failed to deserialize page " + page + " EC data for " + String.valueOf(ownerId) + ": " + e.getMessage() + " - the items are still stored; run /ec repair <player> to recover them.");
                return new ItemStack[0];
            }
        }
        File file = new File(this.dataFolder, this.getPageStorageKey(ownerId, page) + ".data");
        if (!file.exists()) return new ItemStack[0];
        try (BufferedReader reader = new BufferedReader(new FileReader(file));){
            ItemStack[] itemStackArray = EnderChest.itemStackArrayFromBase64(reader.readLine());
            return itemStackArray;
        }
        catch (Exception e) {
            this.loadFailures.add(ownerId);
            this.getLogger().warning("[EnderChest] Failed to deserialize page " + page + " EC data for " + String.valueOf(ownerId) + ": " + e.getMessage() + " - the items are still stored; run /ec repair <player> to recover them.");
        }
        return new ItemStack[0];
    }

    private boolean safeOpenInventory(Player viewer, Inventory inventory) {
        if (inventory == null) {
            viewer.sendMessage(this.prefixed("&cThe EnderChest could not be opened right now."));
            return false;
        }
        if (this.viewerToOwnerMap.containsKey(viewer.getUniqueId())) {
            this.getLogger().warning("[Anti-Exploit] Player " + viewer.getName() + " tried to open multiple EnderChests!");
            viewer.sendMessage(this.prefixed("&cYou already have an EnderChest open!"));
            return false;
        }
        if (!this.activeInventories.containsValue(inventory)) {
            this.getLogger().warning("[Anti-Exploit] Player " + viewer.getName() + " tried to open untracked inventory!");
            return false;
        }
        try {
            viewer.openInventory(inventory);
            return true;
        }
        catch (Exception ex) {
            this.getLogger().warning("Failed to open inventory for " + viewer.getName() + ": " + ex.getMessage());
            viewer.sendMessage(this.prefixed("&cThe EnderChest could not be opened right now."));
            return false;
        }
    }

    private boolean isBackupGui(String title) {
        return BACKUP_MAIN_TITLE.equals(title) || BACKUP_RESTORE_LIST_TITLE.equals(title) || BACKUP_DELETE_LIST_TITLE.equals(title) || BACKUP_MODE_TITLE.equals(title);
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void openBackupMainGui(Player player) {
        if (this.backupManager == null) {
            player.sendMessage(this.prefixed("&cBackup system is not ready yet."));
            return;
        }
        Inventory gui = Bukkit.createInventory(null, (int)27, (String)BACKUP_MAIN_TITLE);
        gui.setItem(11, this.createGuiItem(Material.WRITABLE_BOOK, "\u00a7aCreate Backup", Arrays.asList("\u00a77Click here and type the name in chat.", "\u00a78Type 'cancel' to abort.")));
        gui.setItem(13, this.createGuiItem(Material.RECOVERY_COMPASS, "\u00a7eRestore Backup", Arrays.asList("\u00a77Open the restore list", "\u00a77and choose the restore mode.")));
        gui.setItem(15, this.createGuiItem(Material.LAVA_BUCKET, "\u00a7cDelete Backup", Arrays.asList("\u00a77Delete old backups you no longer need.")));
        gui.setItem(22, this.createGuiItem(this.backupManager.isAutoBackupsEnabled() ? Material.LIME_DYE : Material.GRAY_DYE, this.backupManager.isAutoBackupsEnabled() ? "\u00a7bAuto Backups: \u00a7aON" : "\u00a7bAuto Backups: \u00a7cOFF", Arrays.asList("\u00a77Current time: \u00a7f" + this.backupManager.getAutoBackupTime(), "\u00a77Click to toggle quickly.", "\u00a77Or use /ec autobackup <on|off> [HH:mm].")));
        player.openInventory(gui);
    }

    private void openBackupListGui(Player player, boolean restoreMode) {
        List<BackupManager.BackupInfo> backups = this.backupManager.listBackups();
        Inventory gui = Bukkit.createInventory(null, (int)54, (String)(restoreMode ? BACKUP_RESTORE_LIST_TITLE : BACKUP_DELETE_LIST_TITLE));
        if (backups.isEmpty()) {
            gui.setItem(22, this.createGuiItem(Material.BARRIER, "\u00a7cNo Backups Found", Collections.singletonList("\u00a77Create one in the backup center first.")));
        } else {
            int slot = 0;
            for (BackupManager.BackupInfo info : backups) {
                if (slot >= 45) break;
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(info.getCreatedAt()));
                gui.setItem(slot++, this.createGuiItem(Material.PAPER, "\u00a7d" + info.getName(), Arrays.asList("\u00a77Created: \u00a7f" + date, "\u00a77Chests: \u00a7f" + info.getChestCount(), restoreMode ? "\u00a7aClick to choose a restore mode." : "\u00a7cClick to delete this backup.")));
            }
        }
        gui.setItem(49, this.createGuiItem(Material.ARROW, "\u00a77Back", Collections.singletonList("\u00a77Return to the backup center.")));
        player.openInventory(gui);
    }

    private void openRestoreModeGui(Player player, String backupName) {
        this.selectedBackupRestore.put(player.getUniqueId(), backupName);
        Inventory gui = Bukkit.createInventory(null, (int)27, (String)BACKUP_MODE_TITLE);
        gui.setItem(11, this.createGuiItem(Material.BARRIER, "\u00a7cKick Restore", Arrays.asList("\u00a77All players are kicked first.", "\u00a77Then the backup is restored safely.")));
        gui.setItem(13, this.createGuiItem(Material.PAPER, "\u00a7dBackup: " + backupName, Collections.singletonList("\u00a77Choose one of the two restore modes.")));
        gui.setItem(15, this.createGuiItem(Material.EMERALD, "\u00a7aLive Restore", Arrays.asList("\u00a77Players stay online.", "\u00a77All active ECs close and remain locked until done.")));
        gui.setItem(22, this.createGuiItem(Material.ARROW, "\u00a77Back", Collections.singletonList("\u00a77Return to the restore list.")));
        player.openInventory(gui);
    }

    private void handleBackupInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        String title = event.getView().getTitle();
        if (BACKUP_MAIN_TITLE.equals(title)) {
            if (event.getSlot() == 11) {
                this.pendingBackupNameInput.add(player.getUniqueId());
                player.closeInventory();
                player.sendMessage(this.prefixed("&dType the backup name in chat. Type '&fcancel&d' to abort."));
                return;
            }
            if (event.getSlot() == 13) {
                this.openBackupListGui(player, true);
                return;
            }
            if (event.getSlot() == 15) {
                this.openBackupListGui(player, false);
                return;
            }
            if (event.getSlot() == 22) {
                boolean enabled = !this.backupManager.isAutoBackupsEnabled();
                this.backupManager.setAutoBackupSettings(enabled, this.backupManager.getAutoBackupTime());
                player.sendMessage(this.prefixed("&aAuto backups are now " + (enabled ? "enabled" : "disabled") + "."));
                this.openBackupMainGui(player);
            }
            return;
        }
        if (BACKUP_RESTORE_LIST_TITLE.equals(title)) {
            if (event.getSlot() == 49) {
                this.openBackupMainGui(player);
                return;
            }
            if (item.getType() != Material.PAPER) {
                return;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                this.openRestoreModeGui(player, ChatColor.stripColor((String)meta.getDisplayName()));
            }
            return;
        }
        if (BACKUP_DELETE_LIST_TITLE.equals(title)) {
            if (event.getSlot() == 49) {
                this.openBackupMainGui(player);
                return;
            }
            if (item.getType() != Material.PAPER) {
                return;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String backupName = ChatColor.stripColor((String)meta.getDisplayName());
                if (this.backupManager.deleteBackup(backupName)) {
                    player.sendMessage(this.prefixed("&aBackup '&f" + backupName + "&a' deleted."));
                } else {
                    player.sendMessage(this.prefixed("&cBackup '&f" + backupName + "&c' was not found."));
                }
                this.openBackupListGui(player, false);
            }
            return;
        }
        if (BACKUP_MODE_TITLE.equals(title)) {
            if (event.getSlot() == 22) {
                this.openBackupListGui(player, true);
                return;
            }
            String backupName = this.selectedBackupRestore.remove(player.getUniqueId());
            if (backupName == null) {
                player.sendMessage(this.prefixed("&cNo backup is currently selected."));
                player.closeInventory();
                return;
            }
            player.closeInventory();
            if (event.getSlot() == 11) {
                this.backupManager.restoreBackupAsync(backupName, BackupManager.RestoreMode.KICK_PLAYERS, (CommandSender)player);
            } else if (event.getSlot() == 15) {
                this.backupManager.restoreBackupAsync(backupName, BackupManager.RestoreMode.LIVE_FREEZE, (CommandSender)player);
            }
        }
    }

    void beginGlobalChestOperation(boolean kickPlayers, String playerMessage) {
        this.isMigrating = true;
        this.closeAllInventories();
        for (Player online : new ArrayList<Player>(Bukkit.getOnlinePlayers())) {
            if (kickPlayers) {
                online.kickPlayer(this.prefixed(playerMessage));
                continue;
            }
            if (!this.shouldAnnounceBackupMessages() || playerMessage == null || playerMessage.isBlank()) continue;
            online.sendMessage(this.prefixed(playerMessage));
        }
    }

    void endGlobalChestOperation() {
        this.isMigrating = false;
    }

    boolean isOperationInProgress() {
        return this.isMigrating;
    }

    File getChestDataFolder() {
        return this.dataFolder;
    }

    private void seedPermissionPageCacheFromStorage() {
        HashMap<UUID, Integer> highestStoredPage;
        block26: {
            if (!this.pagesEnabled) {
                return;
            }
            highestStoredPage = new HashMap<UUID, Integer>();
            Pattern pagePattern = Pattern.compile("^(.+)_page(\\d+)$");
            if (this.storageType.equalsIgnoreCase("mysql")) {
                try (Connection conn = this.getSQLConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM " + this.table + " WHERE uuid LIKE ?");){
                    ps.setString(1, "%\\_page%");
                    try (ResultSet rs = ps.executeQuery();){
                        while (rs.next()) {
                            this.recordStoredPage(highestStoredPage, pagePattern, rs.getString("uuid"));
                        }
                        break block26;
                    }
                }
                catch (SQLException e) {
                    this.getLogger().warning("[EnderChest] Could not seed page counts from the database: " + e.getMessage());
                    return;
                }
            }
            File[] files = this.dataFolder.listFiles((dir, name) -> name.endsWith(".data"));
            if (files == null) {
                return;
            }
            for (File file : files) {
                String name2 = file.getName();
                this.recordStoredPage(highestStoredPage, pagePattern, name2.substring(0, name2.length() - ".data".length()));
            }
        }
        if (highestStoredPage.isEmpty()) {
            return;
        }
        this.foliaLib.getScheduler().runNextTick(task -> {
            int seeded = 0;
            for (Map.Entry<UUID, Integer> entry : highestStoredPage.entrySet()) {
                if (this.cachedPermissionPages.containsKey(entry.getKey())) continue;
                int pages = Math.min(Math.max(1, entry.getValue()), this.maxPages);
                this.cachedPermissionPages.put(entry.getKey(), pages);
                this.slotUpgradesConfig.set("cached-permission-pages." + String.valueOf(entry.getKey()), (Object)pages);
                ++seeded;
            }
            if (seeded == 0) {
                return;
            }
            try {
                this.slotUpgradesConfig.save(this.slotUpgradesFile);
                this.getLogger().info("[EnderChest] Restored page counts for " + seeded + " offline player(s) from stored page data.");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void recordStoredPage(Map<UUID, Integer> target, Pattern pagePattern, String storageKey) {
        if (storageKey == null) {
            return;
        }
        Matcher matcher = pagePattern.matcher(storageKey);
        if (!matcher.matches()) {
            return;
        }
        try {
            UUID uuid = UUID.fromString(matcher.group(1));
            int page = Integer.parseInt(matcher.group(2));
            target.merge(uuid, page, Math::max);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
    }

    private void migratePage1Files() {
        if (this.storageType.equalsIgnoreCase("mysql")) {
            return;
        }
        File[] page1Files = this.dataFolder.listFiles((dir, name) -> name.endsWith("_page1.data"));
        if (page1Files == null || page1Files.length == 0) {
            return;
        }
        int migrated = 0;
        for (File page1File : page1Files) {
            String baseName = page1File.getName().replace("_page1.data", "");
            File mainFile = new File(this.dataFolder, baseName + ".data");
            if (!mainFile.exists()) {
                if (page1File.renameTo(mainFile)) {
                    ++migrated;
                    continue;
                }
                this.getLogger().warning("[Migration] Could not rename " + page1File.getName() + " to " + mainFile.getName());
                continue;
            }
            page1File.delete();
            ++migrated;
        }
        if (migrated > 0) {
            this.getLogger().info("[Migration] Migrated " + migrated + " _page1.data files to UUID.data format.");
        }
    }

    public PaperScheduler getFoliaLib() {
        return this.foliaLib;
    }

    String getStorageTypeName() {
        return this.storageType;
    }

    String getTableName() {
        return this.table;
    }

    boolean arePagesEnabled() {
        return this.pagesEnabled;
    }

    FileConfiguration getInvitesConfiguration() {
        return this.invitesConfig;
    }

    File getInvitesFile() {
        return this.invitesFile;
    }

    boolean areTeamsEnabled() {
        return this.teamsEnabled;
    }

    boolean isKnownPlayer(OfflinePlayer player) {
        return player.hasPlayedBefore() || this.namesConfig.contains(player.getUniqueId().toString());
    }

    int getMaxPagesSetting() {
        return this.maxPages;
    }

    void clearLoadFailure(UUID ownerId) {
        this.loadFailures.remove(ownerId);
    }

    boolean isChestBusy(UUID ownerId) {
        return this.activeInventories.containsKey(ownerId) || this.pendingLoads.contains(ownerId) || this.pendingSaves.contains(ownerId);
    }

    void resolveTargetPlayer(CommandSender sender, String name, BiConsumer<UUID, String> callback) {
        Player online = Bukkit.getPlayer((String)name);
        if (online != null) {
            callback.accept(online.getUniqueId(), online.getName());
            return;
        }
        this.resolveOfflinePlayerByName(sender, name, off -> {
            boolean known;
            boolean bl = known = off.hasPlayedBefore() || this.namesConfig.contains(off.getUniqueId().toString());
            if (!known) {
                sender.sendMessage(this.getMessage("player-never-played").replace("{player}", name));
                return;
            }
            callback.accept(off.getUniqueId(), off.getName() != null ? off.getName() : name);
        });
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private boolean tryLockChest(UUID ownerId) {
        if (!this.storageType.equalsIgnoreCase("mysql")) {
            return true;
        }
        try (Connection conn = this.getSQLConnection();){
            boolean bl;
            block22: {
                long now = System.currentTimeMillis();
                String normalizedServerId = this.normalizeServerId(this.serverId);
                String ensureSql = "INSERT INTO " + this.table + " (uuid, data, locked_by, last_lock_time, name) VALUES (?, '', NULL, 0, ?) ON DUPLICATE KEY UPDATE uuid=uuid";
                try (PreparedStatement ps = conn.prepareStatement(ensureSql);){
                    ps.setString(1, ownerId.toString());
                    ps.setString(2, "Unknown");
                    ps.executeUpdate();
                }
                String claimSql = "UPDATE " + this.table + " SET locked_by=?, last_lock_time=? WHERE uuid=? AND (locked_by IS NULL OR locked_by='' OR locked_by=? OR last_lock_time IS NULL OR last_lock_time<=0 OR last_lock_time<? OR last_lock_time>?)";
                PreparedStatement ps = conn.prepareStatement(claimSql);
                try {
                    ps.setString(1, normalizedServerId);
                    ps.setLong(2, now);
                    ps.setString(3, ownerId.toString());
                    ps.setString(4, normalizedServerId);
                    ps.setLong(5, now - 10000L);
                    ps.setLong(6, now + 10000L);
                    boolean bl2 = bl = ps.executeUpdate() > 0;
                    if (ps == null) break block22;
                }
                catch (Throwable throwable) {
                    if (ps != null) {
                        try {
                            ps.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                ps.close();
            }
            return bl;
        }
        catch (SQLException e) {
            return false;
        }
    }

    private void unlockChest(UUID ownerId) {
        if (!this.storageType.equalsIgnoreCase("mysql")) {
            return;
        }
        this.foliaLib.getScheduler().runAsync(task -> {
            try (Connection conn = this.getSQLConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE " + this.table + " SET locked_by=NULL WHERE uuid=? AND locked_by=?");){
                ps.setString(1, ownerId.toString());
                ps.setString(2, this.normalizeServerId(this.serverId));
                ps.executeUpdate();
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
        });
    }

    private void startLockTask(UUID ownerId) {
        if (!this.storageType.equalsIgnoreCase("mysql")) {
            return;
        }
        this.stopLockTask(ownerId);
        PaperTask task = this.foliaLib.getScheduler().runTimer(() -> {
            try (Connection conn = this.getSQLConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE " + this.table + " SET last_lock_time=? WHERE uuid=? AND locked_by=?");){
                ps.setLong(1, System.currentTimeMillis());
                ps.setString(2, ownerId.toString());
                ps.setString(3, this.normalizeServerId(this.serverId));
                ps.executeUpdate();
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
        }, 60L, 60L);
        this.lockTasks.put(ownerId, task);
    }

    private void stopLockTask(UUID ownerId) {
        PaperTask task = this.lockTasks.remove(ownerId);
        if (task != null) {
            task.cancel();
        }
    }

    private void clearStaleLocks() throws SQLException {
        try (Connection conn = this.getSQLConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE " + this.table + " SET locked_by=NULL WHERE locked_by=? OR last_lock_time IS NULL OR last_lock_time<=0 OR last_lock_time<?");){
            ps.setString(1, this.normalizeServerId(this.serverId));
            ps.setLong(2, System.currentTimeMillis() - 10000L);
            ps.executeUpdate();
        }
    }

    Connection getSQLConnection() throws SQLException {
        String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?useSSL=" + this.useSSL + "&autoReconnect=true&allowPublicKeyRetrieval=true";
        return DriverManager.getConnection(url, this.username, this.password);
    }

    private void createTableSync() throws SQLException {
        try (Connection conn = this.getSQLConnection();
             Statement stmt = conn.createStatement();){
            String sql = "CREATE TABLE IF NOT EXISTS " + this.table + " (uuid VARCHAR(36) PRIMARY KEY, data LONGTEXT, locked_by VARCHAR(50), last_lock_time BIGINT, name VARCHAR(32))";
            stmt.executeUpdate(sql);
            try {
                stmt.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN locked_by VARCHAR(50)");
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
            try {
                stmt.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN last_lock_time BIGINT");
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
            try {
                stmt.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN name VARCHAR(32)");
            }
            catch (SQLException sQLException) {
                // empty catch block
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private ItemStack[] loadItemsFromStorage(UUID ownerId) {
        if (this.storageType.equalsIgnoreCase("mysql")) {
            try (Connection conn = this.getSQLConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT data FROM " + this.table + " WHERE uuid=?");){
                ps.setString(1, ownerId.toString());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return null;
                ItemStack[] itemStackArray = EnderChest.itemStackArrayFromBase64(rs.getString("data"));
                return itemStackArray;
            }
            catch (Exception e) {
                this.loadFailures.add(ownerId);
                this.getLogger().warning("[EnderChest] Failed to deserialize EC data for " + String.valueOf(ownerId) + ": " + e.getMessage() + " - the items are still stored; run /ec repair <player> to recover them.");
                return null;
            }
        }
        File file = new File(this.dataFolder, ownerId.toString() + ".data");
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file));){
            ItemStack[] itemStackArray = EnderChest.itemStackArrayFromBase64(reader.readLine());
            return itemStackArray;
        }
        catch (Exception e) {
            this.loadFailures.add(ownerId);
            this.getLogger().warning("[EnderChest] Failed to deserialize EC data for " + String.valueOf(ownerId) + ": " + e.getMessage() + " - the items are still stored; run /ec repair <player> to recover them.");
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void saveInventorySync(UUID ownerId, String ownerName, ItemStack[] items) {
        Object object = this.getSaveLock(ownerId);
        synchronized (object) {
            block24: {
                try {
                    String b64 = EnderChest.itemStackArrayToBase64(items);
                    if (this.storageType.equalsIgnoreCase("mysql")) {
                        try (Connection conn = this.getSQLConnection();
                             PreparedStatement ps = conn.prepareStatement("INSERT INTO " + this.table + " (uuid, data, name) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE data=?, name=?");){
                            ps.setString(1, ownerId.toString());
                            ps.setString(2, b64);
                            ps.setString(3, ownerName);
                            ps.setString(4, b64);
                            ps.setString(5, ownerName);
                            ps.executeUpdate();
                            break block24;
                        }
                    }
                    File file = new File(this.dataFolder, ownerId.toString() + ".data");
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));){
                        writer.write(b64);
                    }
                    if (!ownerName.equals("Unknown")) {
                        this.namesConfig.set(ownerId.toString(), (Object)ownerName);
                        this.namesConfig.save(this.namesFile);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startAutosaveTask() {
        if (this.autosaveTask != null) {
            this.autosaveTask.cancel();
            this.autosaveTask = null;
        }
        if (!this.autosaveEnabled || this.autosaveIntervalSeconds <= 0) {
            return;
        }
        this.autosaveTask = this.foliaLib.getScheduler().runTimer(this::autosaveActiveInventories, (long)this.autosaveIntervalSeconds * 20L, (long)this.autosaveIntervalSeconds * 20L);
    }

    private void autosaveActiveInventories() {
        if (this.isMigrating || this.activeInventories.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, Inventory> entry : this.activeInventories.entrySet()) {
            EnderChestHolder holder;
            UUID ownerId = entry.getKey();
            Inventory inv = entry.getValue();
            if (inv == null || this.pendingSaves.contains(ownerId) || this.pendingLoads.contains(ownerId) || (holder = this.holderOf(inv)) == null || !this.isCurrentGeneration(holder)) continue;
            ItemStack[] itemsToSave = inv.getContents();
            UUID finalOwnerId = ownerId;
            EnderChestHolder finalHolder = holder;
            this.pendingSaves.add(ownerId);
            this.foliaLib.getScheduler().runAsync(task -> {
                block10: {
                    Object object = this.getSaveLock(finalOwnerId);
                    // MONITORENTER : object
                    if (inv.equals((Object)this.activeInventories.get(finalOwnerId))) break block10;
                    // MONITOREXIT : object
                    this.pendingSaves.remove(finalOwnerId);
                    return;
                }
                Long current = this.chestGeneration.get(finalOwnerId);
                if (current != null && current.longValue() != finalHolder.getGeneration()) {
                    // MONITOREXIT : object
                    this.pendingSaves.remove(finalOwnerId);
                    return;
                }
                try {
                    if (finalHolder.isPaged()) {
                        this.savePageItems(finalOwnerId, finalHolder.getPage(), itemsToSave);
                        return;
                    }
                    this.saveInventorySync(finalOwnerId, finalHolder.isTeam() ? finalHolder.getOwnerName() : this.ownerNameFor(finalOwnerId), itemsToSave);
                    // MONITOREXIT : object
                    return;
                }
                finally {
                    this.pendingSaves.remove(finalOwnerId);
                }
            });
        }
    }

    private void checkAutoMigration() {
        File[] files;
        if (this.isMigrating) {
            return;
        }
        if (this.storageType.equalsIgnoreCase("mysql") && (files = this.dataFolder.listFiles((dir, name) -> name.endsWith(".data"))) != null && files.length > 0) {
            this.runMigration("to-db", (CommandSender)Bukkit.getConsoleSender());
        }
    }

    private void runMigration(String mode, CommandSender sender) {
        if (this.isMigrating) {
            return;
        }
        if (mode.equalsIgnoreCase("from-vec")) {
            this.runVariableEnderChestsMigration(sender);
            return;
        }
        if (mode.equalsIgnoreCase("vanilla")) {
            this.runVanillaMigration(sender);
            return;
        }
        if (mode.equalsIgnoreCase("quickec")) {
            this.runQuickECMigration(sender);
            return;
        }
        this.isMigrating = true;
        this.closeAllInventories();
        sender.sendMessage(this.getMessage("migration-started").replace("{from}", mode).replace("{to}", mode.equals("to-db") ? "DB" : "File"));
        this.foliaLib.getScheduler().runAsync(task -> {
            int count = 0;
            int pageCount = 0;
            try {
                if (mode.equalsIgnoreCase("to-db")) {
                    this.createTableSync();
                    File[] files = this.dataFolder.listFiles((dir, name) -> name.endsWith(".data"));
                    if (files != null) {
                        for (File f : files) {
                            String fileName = f.getName().replace(".data", "");
                            String b64 = new BufferedReader(new FileReader(f)).readLine();
                            if (fileName.contains("_page")) {
                                String[] parts = fileName.split("_page");
                                if (parts.length == 2) {
                                    String uuidStr = parts[0];
                                    int page = Integer.parseInt(parts[1]);
                                    String pageUUID = uuidStr + "_page" + page;
                                    try (Connection conn = this.getSQLConnection();
                                         PreparedStatement ps = conn.prepareStatement("INSERT INTO " + this.table + " (uuid, data, name) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE data=?, name=?");){
                                        ps.setString(1, pageUUID);
                                        ps.setString(2, b64);
                                        ps.setString(3, "Page" + page);
                                        ps.setString(4, b64);
                                        ps.setString(5, "Page" + page);
                                        ps.executeUpdate();
                                    }
                                    ++pageCount;
                                }
                            } else {
                                this.saveInventorySync(UUID.fromString(fileName), "Migrated", EnderChest.itemStackArrayFromBase64(b64));
                                ++count;
                            }
                            Files.move(f.toPath(), new File(this.dataFolder, fileName + ".data.bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } else if (mode.equalsIgnoreCase("to-file")) {
                    try (Connection conn = this.getSQLConnection();
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM " + this.table);){
                        while (rs.next()) {
                            String uuid = rs.getString("uuid");
                            String name2 = rs.getString("name");
                            String data = rs.getString("data");
                            if (uuid.contains("_page")) {
                                String[] parts = uuid.split("_page");
                                if (parts.length != 2) continue;
                                File file = new File(this.dataFolder, uuid + ".data");
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));){
                                    writer.write(data);
                                }
                                ++pageCount;
                                continue;
                            }
                            this.saveInventorySync(UUID.fromString(uuid), name2, EnderChest.itemStackArrayFromBase64(data));
                            ++count;
                        }
                    }
                }
                sender.sendMessage(this.getMessage("migration-finished").replace("{count}", String.valueOf(count + pageCount)));
                if (pageCount > 0) {
                    sender.sendMessage(this.getMessage("prefix") + "\u00a77(\u00a7f" + count + "\u00a77 main + \u00a7f" + pageCount + "\u00a77 pages)");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(this.getMessage("migration-failed"));
            }
            finally {
                this.isMigrating = false;
            }
        });
    }

    private void runQuickECMigration(CommandSender sender) {
        sender.sendMessage(this.prefixed("&7QuickEC stores nothing of its own - it opens the vanilla Ender Chest,"));
        sender.sendMessage(this.prefixed("&7so its items are the vanilla ones. Running the vanilla import."));
        if (this.isQuickECInstalled()) {
            sender.sendMessage(this.prefixed("&eQuickEC is still installed. Remove it after the import:"));
            sender.sendMessage(this.prefixed("&eit registers &f/ec &etoo, and it opens the vanilla chest, which"));
            sender.sendMessage(this.prefixed("&elets players reach the items you just imported."));
        }
        this.runVanillaMigration(sender);
    }

    private boolean isQuickECInstalled() {
        return Bukkit.getPluginManager().getPlugin("QuickEC") != null;
    }

    private void warnAboutQuickEC() {
        if (!this.isQuickECInstalled()) {
            return;
        }
        this.sendConsole("\u00a7e [!] \u00a7fQuickEC is installed alongside this plugin.");
        this.sendConsole("\u00a7e     Both register \u00a7f/ec\u00a7e, and QuickEC opens the vanilla Ender Chest,");
        this.sendConsole("\u00a7e     which bypasses sizes, pages, blacklist and invites.");
        this.sendConsole("\u00a7e     Import the vanilla data with \u00a7f/ec migrate quickec\u00a7e, then remove QuickEC.");
    }

    private void runVanillaMigration(CommandSender sender) {
        sender.sendMessage(this.getMessage("prefix") + "\u00a7eStarting Vanilla migration (online + offline)...");
        sender.sendMessage(this.getMessage("prefix") + "\u00a77This may take a moment for offline players.");
        this.isMigrating = true;
        this.closeAllInventories();
        ArrayList<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) {
            this.foliaLib.getScheduler().runAsync(task2 -> this.runVanillaMigrationOffline(sender, 0, Collections.emptySet()));
            return;
        }
        AtomicInteger onlineMigratedCount = new AtomicInteger(0);
        ConcurrentHashMap.KeySetView onlineUUIDs = ConcurrentHashMap.newKeySet();
        ArrayList<CompletableFuture<?>> pending = new ArrayList<CompletableFuture<?>>();
        for (Player p : onlinePlayers) {
            onlineUUIDs.add(p.getUniqueId());
            pending.add(this.foliaLib.getScheduler().runAtEntity((Entity)p, task -> {
                Inventory vanillaInv = p.getEnderChest();
                boolean hasItems = false;
                for (ItemStack is : vanillaInv.getContents()) {
                    if (is == null || is.getType() == Material.AIR) continue;
                    hasItems = true;
                    break;
                }
                if (hasItems) {
                    this.saveInventorySync(p.getUniqueId(), p.getName(), vanillaInv.getContents());
                    vanillaInv.clear();
                    onlineMigratedCount.incrementAndGet();
                    p.sendMessage(this.getMessage("prefix") + "\u00a7aYour vanilla EnderChest has been imported!");
                }
            }));
        }
        this.whenAllSettled(pending, "VanillaMigration").thenRun(() -> this.foliaLib.getScheduler().runAsync(task2 -> this.runVanillaMigrationOffline(sender, onlineMigratedCount.get(), onlineUUIDs)));
    }

    private CompletableFuture<Void> whenAllSettled(List<CompletableFuture<?>> tasks, String logTag) {
        CompletableFuture<Void> gate = new CompletableFuture<Void>();
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).whenComplete((result, error) -> gate.complete(null));
        this.foliaLib.getScheduler().runLaterAsync(() -> {
            if (gate.complete(null)) {
                this.getLogger().warning("[" + logTag + "] Online phase did not finish within 60s; continuing anyway.");
            }
        }, 1200L);
        return gate;
    }

    private static String presentKey(NBTCompound compound, String[] candidates) {
        for (String key : candidates) {
            if (!compound.hasTag(key)) continue;
            return key;
        }
        return null;
    }

    private static int readSlot(NBTListCompound entry, int fallback) {
        String key = EnderChest.presentKey(entry, SLOT_KEYS);
        if (key == null) {
            return fallback;
        }
        if (entry.getType(key) == NBTType.NBTTagInt) {
            Integer slot = entry.getInteger(key);
            return slot == null ? fallback : slot;
        }
        Byte slot = entry.getByte(key);
        return slot == null ? fallback : slot & 0xFF;
    }

    private File[] listPlayerDataFiles(String logTag) {
        File playerDataFolder = new File(((World)Bukkit.getWorlds().get(0)).getWorldFolder(), "playerdata");
        File[] datFiles = playerDataFolder.listFiles((dir, name) -> name.endsWith(".dat"));
        if (datFiles == null) {
            this.getLogger().warning("[" + logTag + "] No playerdata folder at " + playerDataFolder.getAbsolutePath());
            return new File[0];
        }
        return datFiles;
    }

    private static UUID uuidFromDatFile(File datFile) {
        String name = datFile.getName();
        try {
            return UUID.fromString(name.substring(0, name.length() - ".dat".length()));
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void runVanillaMigrationOffline(CommandSender sender, int onlineCount, Set<UUID> onlineUUIDs) {
        int offlineCount = 0;
        int scanned = 0;
        int withoutItems = 0;
        int failed = 0;
        for (File datFile : this.listPlayerDataFiles("VanillaMigration")) {
            UUID ownerId = EnderChest.uuidFromDatFile(datFile);
            if (ownerId == null || onlineUUIDs.contains(ownerId)) continue;
            ++scanned;
            try {
                NBTFile playerData = new NBTFile(datFile);
                String enderKey = EnderChest.presentKey(playerData, ENDER_ITEMS_KEYS);
                if (enderKey == null) {
                    ++withoutItems;
                    continue;
                }
                NBTCompoundList enderItems = playerData.getCompoundList(enderKey);
                if (enderItems == null || enderItems.size() == 0) {
                    ++withoutItems;
                    continue;
                }
                ItemStack[] items = new ItemStack[27];
                boolean hasItems = false;
                for (int i = 0; i < enderItems.size(); ++i) {
                    ItemStack item;
                    NBTListCompound compound = enderItems.get(i);
                    int slot = EnderChest.readSlot(compound, i);
                    if (slot < 0 || slot >= 27 || (item = NBTItem.convertNBTtoItem(compound)) == null || item.getType() == Material.AIR) continue;
                    items[slot] = item;
                    hasItems = true;
                }
                if (hasItems) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer((UUID)ownerId);
                    String name = op.getName() != null ? op.getName() : ownerId.toString();
                    this.saveInventorySync(ownerId, name, items);
                    playerData.removeKey(enderKey);
                    playerData.save();
                    ++offlineCount;
                    continue;
                }
                ++withoutItems;
            }
            catch (Exception e) {
                ++failed;
                this.getLogger().warning("[VanillaMigration] Could not migrate " + datFile.getName() + ": " + e.getMessage());
            }
        }
        this.isMigrating = false;
        int total = onlineCount + offlineCount;
        this.getLogger().info("[VanillaMigration] Offline phase: " + scanned + " playerdata files scanned, " + offlineCount + " imported, " + withoutItems + " with an empty ender chest, " + failed + " failed.");
        sender.sendMessage(this.getMessage("prefix") + "\u00a7aMigration finished. Imported: " + total + " players (" + onlineCount + " online, " + offlineCount + " offline).");
        if (scanned > 0 && offlineCount == 0) {
            sender.sendMessage(this.getMessage("prefix") + "\u00a77Scanned " + scanned + " offline playerdata files; none held ender chest items. See the console for details.");
        }
    }

    private void runExport(CommandSender sender) {
        sender.sendMessage(this.getMessage("prefix") + "\u00a7eStarting EXPORT to Vanilla EnderChests (online + offline)...");
        sender.sendMessage(this.getMessage("prefix") + "\u00a7cWARNING: Only Page 1 (first 27 slots) will be exported to Vanilla!");
        sender.sendMessage(this.getMessage("prefix") + "\u00a7cAdditional pages and items beyond slot 27 will be LOST!");
        this.isMigrating = true;
        this.closeAllInventories();
        ArrayList<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) {
            this.foliaLib.getScheduler().runAsync(task2 -> this.runExportOffline(sender, 0, Collections.emptySet()));
            return;
        }
        AtomicInteger onlineExportedCount = new AtomicInteger(0);
        ConcurrentHashMap.KeySetView onlineUUIDs = ConcurrentHashMap.newKeySet();
        ArrayList<CompletableFuture<?>> pending = new ArrayList<CompletableFuture<?>>();
        for (Player p : onlinePlayers) {
            onlineUUIDs.add(p.getUniqueId());
            pending.add(this.foliaLib.getScheduler().runAtEntity((Entity)p, task -> {
                ItemStack[] customItems;
                ItemStack[] itemStackArray = customItems = this.pagesEnabled ? this.loadPageItems(p.getUniqueId(), 1) : this.loadItemsFromStorage(p.getUniqueId());
                if (customItems == null || customItems.length == 0) {
                    return;
                }
                Inventory vanillaInv = p.getEnderChest();
                vanillaInv.clear();
                int maxSlots = 27;
                ItemStack[] toFit = Arrays.copyOf(customItems, Math.min(customItems.length, maxSlots));
                vanillaInv.setContents(toFit);
                if (customItems.length > maxSlots) {
                    for (int i = maxSlots; i < customItems.length; ++i) {
                        if (customItems[i] == null || customItems[i].getType() == Material.AIR) continue;
                        p.getWorld().dropItem(p.getLocation(), customItems[i]);
                    }
                    p.sendMessage(this.getMessage("prefix") + "\u00a7cYour EnderChest was too big for Vanilla. Excess items dropped at your feet.");
                }
                onlineExportedCount.incrementAndGet();
            }));
        }
        this.whenAllSettled(pending, "Export").thenRun(() -> this.foliaLib.getScheduler().runAsync(task2 -> this.runExportOffline(sender, onlineExportedCount.get(), onlineUUIDs)));
    }

    private void runExportOffline(CommandSender sender, int onlineCount, Set<UUID> onlineUUIDs) {
        int offlineCount = 0;
        File playerDataFolder = new File(((World)Bukkit.getWorlds().get(0)).getWorldFolder(), "playerdata");
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            File datFile;
            ItemStack[] customItems;
            if (onlineUUIDs.contains(op.getUniqueId())) continue;
            ItemStack[] itemStackArray = customItems = this.pagesEnabled ? this.loadPageItems(op.getUniqueId(), 1) : this.loadItemsFromStorage(op.getUniqueId());
            if (customItems == null) continue;
            boolean hasItems = false;
            for (ItemStack is : customItems) {
                if (is == null || is.getType() == Material.AIR) continue;
                hasItems = true;
                break;
            }
            if (!hasItems || !(datFile = new File(playerDataFolder, String.valueOf(op.getUniqueId()) + ".dat")).exists()) continue;
            try {
                NBTFile playerData = new NBTFile(datFile);
                String enderKey = EnderChest.presentKey(playerData, ENDER_ITEMS_KEYS);
                if (enderKey == null) {
                    enderKey = playerData.hasTag("inventory") ? "ender_items" : "EnderItems";
                }
                boolean snakeCase = "ender_items".equals(enderKey);
                playerData.removeKey(enderKey);
                NBTCompoundList list = playerData.getCompoundList(enderKey);
                int maxSlots = 27;
                int deletedCount = 0;
                for (int i = 0; i < customItems.length; ++i) {
                    ItemStack item = customItems[i];
                    if (item == null || item.getType() == Material.AIR) continue;
                    if (i < maxSlots) {
                        NBTContainer itemNbt = NBTItem.convertItemtoNBT(item);
                        NBTListCompound entry = list.addCompound();
                        entry.mergeCompound((ReadableNBT)itemNbt);
                        if (snakeCase) {
                            entry.setInteger("slot", i);
                            continue;
                        }
                        entry.setByte("Slot", (byte)i);
                        continue;
                    }
                    ++deletedCount;
                }
                playerData.save();
                ++offlineCount;
                if (deletedCount <= 0) continue;
                this.getLogger().warning("[Export] " + op.getName() + " had " + deletedCount + " items beyond slot 27 \u2014 DELETED (player offline).");
            }
            catch (Exception e) {
                this.getLogger().warning("[Export] Could not export " + String.valueOf(op.getUniqueId()) + ": " + e.getMessage());
            }
        }
        this.isMigrating = false;
        int total = onlineCount + offlineCount;
        sender.sendMessage(this.getMessage("prefix") + "\u00a7aExport finished. Exported: " + total + " players (" + onlineCount + " online, " + offlineCount + " offline).");
    }

    private void runVariableEnderChestsMigration(CommandSender sender) {
        Plugin vecPlugin = Bukkit.getPluginManager().getPlugin("VariableEnderChests");
        if (vecPlugin == null || !vecPlugin.isEnabled()) {
            sender.sendMessage(this.getMessage("migration-vec-not-found"));
            return;
        }
        sender.sendMessage(this.getMessage("migration-vec-start"));
        this.isMigrating = true;
        this.closeAllInventories();
        this.foliaLib.getScheduler().runAsync(task -> {
            int count = 0;
            int skipped = 0;
            int emptyImported = 0;
            try {
                Class mainClass = vecPlugin.getClass();
                Method getManagerMethod = mainClass.getMethod("getEnderChestManager", new Class[0]);
                Object managerObj = getManagerMethod.invoke((Object)vecPlugin, new Object[0]);
                if (managerObj == null) {
                    sender.sendMessage(this.getMessage("migration-failed") + " Could not get EnderChestManager from VariableEnderChests");
                    return;
                }
                Class<?> managerClass = managerObj.getClass();
                List<OfflinePlayer> targets = this.resolveVecMigrationTargets(vecPlugin, sender);
                sender.sendMessage(this.prefixed("&7Scanning &f" + targets.size() + "&7 candidate players from VEC data..."));
                for (OfflinePlayer op : targets) {
                    try {
                        UUID uuid = op.getUniqueId();
                        if (uuid == null) {
                            ++skipped;
                            continue;
                        }
                        ItemStack[] contents = this.fetchVecContentsWithRetry(managerObj, managerClass, op, 5, 75L);
                        if (contents == null) {
                            ++skipped;
                            continue;
                        }
                        String playerName = op.getName();
                        if (playerName == null || playerName.isBlank()) {
                            playerName = "Imported";
                        }
                        if (!this.containsAnyRealItem(contents)) {
                            ++emptyImported;
                        }
                        this.saveInventorySync(uuid, playerName, contents);
                        ++count;
                    }
                    catch (Exception ex) {
                        ++skipped;
                        this.getLogger().warning("[VEC Migration] Error importing chest for " + String.valueOf(op.getUniqueId()) + ": " + ex.getMessage());
                    }
                    if ((count + skipped) % 50 != 0 || count + skipped <= 0) continue;
                    sender.sendMessage("\u00a7eProgress: " + count + " imported, " + skipped + " skipped...");
                }
                sender.sendMessage(this.getMessage("migration-finished").replace("{count}", String.valueOf(count)));
                if (skipped > 0) {
                    sender.sendMessage(this.prefixed("&7(&a" + count + "&7 imported, &c" + skipped + "&7 skipped)"));
                }
                if (emptyImported > 0) {
                    sender.sendMessage(this.prefixed("&7(&e" + emptyImported + "&7 empty chests were imported)"));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(this.getMessage("migration-failed") + " " + e.getMessage());
            }
            finally {
                this.isMigrating = false;
            }
        });
    }

    private List<OfflinePlayer> resolveVecMigrationTargets(Plugin vecPlugin, CommandSender sender) {
        Set<UUID> candidateUuids = this.collectVecCandidateUuids(vecPlugin.getDataFolder());
        LinkedHashSet<UUID> selected = new LinkedHashSet<UUID>();
        for (UUID uuid : candidateUuids) {
            OfflinePlayer op = Bukkit.getOfflinePlayer((UUID)uuid);
            if (op == null) continue;
            String string = op.getName();
            if (!op.isOnline() && (!op.hasPlayedBefore() || string == null || string.isBlank())) continue;
            selected.add(uuid);
        }
        if (sender instanceof Player) {
            Player senderPlayer = (Player)sender;
            selected.add(senderPlayer.getUniqueId());
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            selected.add(online.getUniqueId());
        }
        if (selected.isEmpty()) {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer == null || offlinePlayer.getUniqueId() == null) continue;
                String name = offlinePlayer.getName();
                if (!offlinePlayer.isOnline() && (!offlinePlayer.hasPlayedBefore() || name == null || name.isBlank())) continue;
                selected.add(offlinePlayer.getUniqueId());
            }
        }
        return selected.stream().map(Bukkit::getOfflinePlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Set<UUID> collectVecCandidateUuids(File vecDataFolder) {
        HashSet<UUID> uuids = new HashSet<UUID>();
        if (vecDataFolder == null || !vecDataFolder.exists()) {
            return uuids;
        }
        try (Stream<Path> stream = Files.walk(vecDataFolder.toPath(), new FileVisitOption[0]);){
            stream.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).forEach(path -> {
                String fileName = path.getFileName().toString();
                Matcher matcher = UUID_IN_FILENAME_PATTERN.matcher(fileName);
                while (matcher.find()) {
                    try {
                        uuids.add(UUID.fromString(matcher.group()));
                    }
                    catch (IllegalArgumentException illegalArgumentException) {}
                }
            });
        }
        catch (IOException ex) {
            this.getLogger().warning("[VEC Migration] Could not scan VariableEnderChests folder: " + ex.getMessage());
        }
        return uuids;
    }

    private ItemStack[] fetchVecContentsWithRetry(Object managerObj, Class<?> managerClass, OfflinePlayer player, int retries, long waitMs) throws InterruptedException {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        for (int attempt = 1; attempt <= retries; ++attempt) {
            block4: {
                try {
                    ItemStack[] items = this.fetchVecContentsSync(managerObj, managerClass, uuid, playerName);
                    if (items != null) {
                        return items;
                    }
                }
                catch (Exception ex) {
                    if (attempt < retries) break block4;
                    this.getLogger().warning("[VEC Migration] Could not fetch chest " + String.valueOf(uuid) + " after " + retries + " attempts: " + ex.getMessage());
                    break;
                }
            }
            if (attempt >= retries) continue;
            Thread.sleep(waitMs);
        }
        return null;
    }

    private ItemStack[] fetchVecContentsSync(Object managerObj, Class<?> managerClass, UUID uuid, String playerName) throws Exception {
        Method byNameCallbackMethod;
        Method directMethod = this.findVecDirectGetEnderChestMethod(managerClass);
        if (directMethod != null) {
            Object ecObj = directMethod.invoke(managerObj, uuid);
            return this.extractVecInventoryContents(ecObj);
        }
        Method callbackMethod = this.findVecCallbackGetEnderChestMethod(managerClass);
        if (callbackMethod == null) {
            return null;
        }
        Object callbackObj = callbackMethod.invoke(managerObj, uuid);
        Object ecObj = this.awaitVecCallbackResult(callbackObj, managerObj, uuid, 20, 100L);
        if (ecObj == null && playerName != null && !playerName.isBlank() && (byNameCallbackMethod = this.findVecNameCallbackGetEnderChestMethod(managerClass)) != null) {
            Object byNameCallbackObj = byNameCallbackMethod.invoke(managerObj, playerName);
            ecObj = this.awaitVecCallbackResult(byNameCallbackObj, managerObj, uuid, 20, 100L);
        }
        return this.extractVecInventoryContents(ecObj);
    }

    private Method findVecDirectGetEnderChestMethod(Class<?> managerClass) {
        for (Method method : managerClass.getMethods()) {
            String returnTypeName;
            Class<?> returnType;
            Class<?>[] params;
            if (!"getEnderChest".equals(method.getName()) || (params = method.getParameterTypes()).length != 1 || !UUID.class.equals(params[0]) || Void.TYPE.equals(returnType = method.getReturnType()) || (returnTypeName = returnType.getName()).endsWith("Callback") || returnTypeName.contains(".utils.Callback")) continue;
            return method;
        }
        return null;
    }

    private Method findVecCallbackGetEnderChestMethod(Class<?> managerClass) {
        for (Method method : managerClass.getMethods()) {
            String returnTypeName;
            Class<?>[] params;
            if (!"getEnderChest".equals(method.getName()) || (params = method.getParameterTypes()).length != 1 || !UUID.class.equals(params[0]) || !(returnTypeName = method.getReturnType().getName()).endsWith("Callback") && !returnTypeName.contains(".utils.Callback")) continue;
            return method;
        }
        return null;
    }

    private Method findVecNameCallbackGetEnderChestMethod(Class<?> managerClass) {
        for (Method method : managerClass.getMethods()) {
            String returnTypeName;
            Class<?>[] params;
            if (!"getEnderChest".equals(method.getName()) || (params = method.getParameterTypes()).length != 1 || !String.class.equals(params[0]) || !(returnTypeName = method.getReturnType().getName()).endsWith("Callback") && !returnTypeName.contains(".utils.Callback")) continue;
            return method;
        }
        return null;
    }

    private Object awaitVecCallbackResult(Object callbackObj, Object managerObj, UUID uuid, int retries, long waitMs) throws Exception {
        if (callbackObj == null) {
            return this.resolveVecChestFromManagerState(managerObj, uuid);
        }
        Method hasResultsMethod = callbackObj.getClass().getMethod("hasResults", new Class[0]);
        Method getResultMethod = callbackObj.getClass().getMethod("getResult", new Class[0]);
        for (int i = 0; i < retries; ++i) {
            boolean hasResults = Boolean.TRUE.equals(hasResultsMethod.invoke(callbackObj, new Object[0]));
            if (hasResults) {
                return getResultMethod.invoke(callbackObj, new Object[0]);
            }
            Thread.sleep(waitMs);
        }
        return this.resolveVecChestFromManagerState(managerObj, uuid);
    }

    private Object resolveVecChestFromManagerState(Object managerObj, UUID uuid) {
        try {
            Object value;
            Method direct = this.findVecDirectGetEnderChestMethod(managerObj.getClass());
            if (direct != null && (value = direct.invoke(managerObj, uuid)) != null) {
                return value;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        for (Field field : managerObj.getClass().getDeclaredFields()) {
            if (!Map.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                Object mapObj = field.get(managerObj);
                if (!(mapObj instanceof Map)) continue;
                Map map = (Map)mapObj;
                Object value = map.get(uuid);
                if (value == null) {
                    value = map.get(uuid.toString());
                }
                if (value == null) continue;
                return value;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return null;
    }

    private ItemStack[] extractVecInventoryContents(Object ecObj) throws Exception {
        if (ecObj == null) {
            return null;
        }
        if (ecObj instanceof Inventory) {
            ItemStack[] contents = ((Inventory)ecObj).getContents();
            return contents == null ? null : Arrays.copyOf(contents, contents.length);
        }
        Method getInvMethod = ecObj.getClass().getMethod("getInventory", new Class[0]);
        Object invObj = getInvMethod.invoke(ecObj, new Object[0]);
        if (!(invObj instanceof Inventory)) {
            return null;
        }
        ItemStack[] contents = ((Inventory)invObj).getContents();
        return contents == null ? null : Arrays.copyOf(contents, contents.length);
    }

    private boolean containsAnyRealItem(ItemStack[] contents) {
        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) continue;
            return true;
        }
        return false;
    }

    private void loadNamesFromFile() {
        this.knownPlayerNames.clear();
        for (String key : this.namesConfig.getKeys(false)) {
            this.knownPlayerNames.add(this.namesConfig.getString(key));
        }
    }

    private void loadNamesFromMySQL() {
        try (Connection conn = this.getSQLConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM " + this.table);){
            while (rs.next()) {
                this.knownPlayerNames.add(rs.getString("name"));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static String itemStackArrayToBase64(ItemStack[] items) throws IOException {
        return ItemStorageCodec.encode(items);
    }

    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException, ClassNotFoundException {
        return ItemStorageCodec.decode(data);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        this.foliaLib.getScheduler().runAtEntityLater((Entity)joined, () -> {
            if (joined.isOnline()) {
                this.getChestRows(joined, false);
                if (this.pagesEnabled) {
                    this.getPlayerMaxPages(joined);
                }
            }
        }, 20L);
    }

    @EventHandler(priority=EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ENDER_CHEST) {
            Player p = event.getPlayer();
            if (this.hasVanillaBypass(p)) {
                return;
            }
            if (!this.canOpenPhysicalEnderChest(p)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void clearPlayerData(UUID targetUUID, Integer page) {
        block35: {
            if (page == null) {
                Inventory activeInventory = this.activeInventories.remove(targetUUID);
                this.activeInventoryPage.remove(targetUUID);
                if (activeInventory != null) {
                    for (HumanEntity human : new ArrayList<HumanEntity>(activeInventory.getViewers())) {
                        if (!(human instanceof Player)) continue;
                        Player openViewer = (Player)human;
                        this.viewerToOwnerMap.remove(openViewer.getUniqueId());
                        this.playerCurrentPage.remove(openViewer.getUniqueId());
                        openViewer.closeInventory();
                        openViewer.sendMessage(this.prefixed("&eYour EnderChest was cleared by an admin."));
                    }
                }
                this.stopLockTask(targetUUID);
                this.unlockChest(targetUUID);
            } else {
                Integer activePage = this.activeInventoryPage.getOrDefault(targetUUID, 1);
                if (activePage.equals(page)) {
                    Inventory activeInventory = this.activeInventories.remove(targetUUID);
                    this.activeInventoryPage.remove(targetUUID);
                    if (activeInventory != null) {
                        for (HumanEntity human : new ArrayList<HumanEntity>(activeInventory.getViewers())) {
                            if (!(human instanceof Player)) continue;
                            Player openViewer = (Player)human;
                            this.viewerToOwnerMap.remove(openViewer.getUniqueId());
                            this.playerCurrentPage.remove(openViewer.getUniqueId());
                            openViewer.closeInventory();
                            openViewer.sendMessage(this.prefixed("&ePage " + page + " of your EnderChest was cleared by an admin."));
                        }
                    }
                    this.stopLockTask(targetUUID);
                    this.unlockChest(targetUUID);
                }
            }
            if (this.storageType.equalsIgnoreCase("mysql")) {
                try (Connection conn = this.getSQLConnection();){
                    if (page == null) {
                        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + this.table + " WHERE uuid = ? OR uuid LIKE ?");){
                            ps.setString(1, targetUUID.toString());
                            ps.setString(2, targetUUID.toString() + "_page%");
                            ps.executeUpdate();
                            break block35;
                        }
                    }
                    String pageUUID = this.getPageStorageKey(targetUUID, page);
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + this.table + " WHERE uuid = ?");){
                        ps.setString(1, pageUUID);
                        ps.executeUpdate();
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (page == null) {
                File[] files = this.dataFolder.listFiles((dir, name) -> name.startsWith(targetUUID.toString()));
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            } else {
                File file = new File(this.dataFolder, this.getPageStorageKey(targetUUID, page) + ".data");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void clearAllPlayerData() {
        for (UUID ownerId : new ArrayList<UUID>(this.activeInventories.keySet())) {
            Inventory inv = this.activeInventories.get(ownerId);
            if (inv != null) {
                for (HumanEntity viewer : new ArrayList<HumanEntity>(inv.getViewers())) {
                    if (!(viewer instanceof Player)) continue;
                    Player player = (Player)viewer;
                    this.viewerToOwnerMap.remove(player.getUniqueId());
                    this.playerCurrentPage.remove(player.getUniqueId());
                    player.closeInventory();
                    player.sendMessage(this.prefixed("&eAll EnderChests were cleared by an admin."));
                }
            }
            this.stopLockTask(ownerId);
            this.unlockChest(ownerId);
        }
        this.activeInventories.clear();
        this.viewerToOwnerMap.clear();
        this.pendingViewerOpens.clear();
        if (this.storageType.equalsIgnoreCase("mysql")) {
            try (Connection conn = this.getSQLConnection();
                 Statement stmt = conn.createStatement();){
                stmt.executeUpdate("DELETE FROM " + this.table);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            File[] files = this.dataFolder.listFiles((dir, name) -> name.endsWith(".data"));
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

}
