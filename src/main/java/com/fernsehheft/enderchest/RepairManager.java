package com.fernsehheft.enderchest;

import com.fernsehheft.enderchest.EnderChest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RepairManager {
    private static final String WRAPPER_CLASS = "org.bukkit.util.io.Wrapper";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String META_DROPPED = "EC_REPAIR_META_DROPPED";
    private static final String[][] META_STRIP_STEPS = new String[][]{{"internal"}, {"PublicBukkitValues", "custom-data"}, {"enchants", "stored-enchants"}, {"attribute-modifiers"}, {"components", "custom-model-data"}, {"BlockEntityTag", "BlockStateTag", "bukkit-entity-tag", "entity-tag", "items", "charged-projectiles"}};
    private static final List<String> META_SAFE_KEYS = Arrays.asList("==", "meta-type", "display-name", "custom-name", "item-name", "lore", "Damage", "damage", "repair-cost", "Unbreakable", "ItemFlags");
    private static final int ROWS_PER_PAGE = 8;
    private static final int MAX_SLOT_LINES = 8;
    private static final Pattern SNAPSHOT_NAME = Pattern.compile("^(.+?)_(\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2})\\.data$");
    private final EnderChest plugin;

    public RepairManager(EnderChest plugin) {
        this.plugin = plugin;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PageReport read(String base64, int page, String storageKey) {
        PageReport pageReport;
        byte[] raw;
        if (base64 == null || base64.trim().isEmpty()) {
            return new PageReport(page, storageKey, base64, new ItemStack[0]);
        }
        try {
            raw = RepairManager.decode(base64.trim());
        }
        catch (Exception e) {
            PageReport failed = new PageReport(page, storageKey, base64, null);
            failed.fatalError = "the stored text is not valid base64 (" + RepairManager.rootMessage(e) + ")";
            return failed;
        }
        ObjectInputStream in = null;
        try {
            in = new SalvagingInputStream(new ByteArrayInputStream(raw));
            int declared = in.readInt();
            if (declared < 0 || declared > 1024) {
                PageReport failed = new PageReport(page, storageKey, base64, null);
                failed.fatalError = "the stored slot count is implausible (" + declared + ")";
                PageReport pageReport2 = failed;
                return pageReport2;
            }
            ItemStack[] items = new ItemStack[declared];
            PageReport report = new PageReport(page, storageKey, base64, items);
            report.declaredSlots = declared;
            for (int slot = 0; slot < declared; ++slot) {
                ((SalvagingInputStream)in).notes.clear();
                ((SalvagingInputStream)in).lastItemName = null;
                try {
                    Object value = in.readObject();
                    if (value instanceof ItemStack) {
                        items[slot] = (ItemStack)value;
                        ++report.recoveredItems;
                    } else if (value != null) {
                        report.problems.add(new SlotReport(slot, Outcome.LOST, value.getClass().getSimpleName(), "the stored object was not an item"));
                        continue;
                    }
                    if (((SalvagingInputStream)in).notes.isEmpty()) continue;
                    report.problems.add(new SlotReport(slot, Outcome.REPAIRED, RepairManager.describe(items[slot], ((SalvagingInputStream)in).lastItemName), String.join((CharSequence)"; ", ((SalvagingInputStream)in).notes)));
                    continue;
                }
                catch (Throwable t) {
                    report.problems.add(new SlotReport(slot, Outcome.LOST, ((SalvagingInputStream)in).lastItemName, RepairManager.rootMessage(t)));
                    if (!RepairManager.isStreamDesync(t)) continue;
                    report.problems.add(new SlotReport(slot + 1, Outcome.TRUNCATED, null, RepairManager.rootMessage(t)));
                    break;
                }
            }
            pageReport = report;
        }
        catch (Exception e) {
            PageReport failed = new PageReport(page, storageKey, base64, null);
            failed.fatalError = RepairManager.rootMessage(e);
            PageReport pageReport3 = failed;
            return pageReport3;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException iOException) {}
            }
        }
        return pageReport;
    }

    private static byte[] decode(String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        }
        catch (IllegalArgumentException e) {
            String cleaned = base64.replaceAll("[^A-Za-z0-9+/=]", "");
            int usable = cleaned.length() - cleaned.length() % 4;
            return Base64.getMimeDecoder().decode(cleaned.substring(0, usable));
        }
    }

    private static boolean isStreamDesync(Throwable t) {
        for (Throwable cause = t; cause != null; cause = cause.getCause()) {
            if (cause instanceof StreamCorruptedException || cause instanceof EOFException || cause instanceof OptionalDataException) {
                return true;
            }
            if (cause.getCause() == cause) break;
        }
        return false;
    }

    private static String describe(ItemStack item, String fallback) {
        return item != null ? item.getType().name() + " x" + item.getAmount() : fallback;
    }

    static String rootMessage(Throwable t) {
        Throwable root;
        for (root = t; root.getCause() != null && root.getCause() != root; root = root.getCause()) {
        }
        String message = root.getMessage();
        return root.getClass().getSimpleName() + (String)(message == null ? "" : ": " + message);
    }

    private static Object salvage(Map<String, Object> map, List<String> notes) {
        return map.containsKey("type") ? RepairManager.salvageItem(map, notes) : RepairManager.salvageMeta(map, notes);
    }

    private static Object salvageItem(Map<String, Object> map, List<String> notes) {
        Material material;
        String type = String.valueOf(map.get("type"));
        if (map.containsKey("meta")) {
            LinkedHashMap<String, Object> withoutMeta = new LinkedHashMap<String, Object>(map);
            withoutMeta.remove("meta");
            try {
                ConfigurationSerializable result = ConfigurationSerialization.deserializeObject(withoutMeta);
                if (result != null) {
                    notes.add("dropped all item meta (name, lore, enchantments)");
                    return result;
                }
            }
            catch (Throwable result) {
                // empty catch block
            }
        }
        if ((material = Material.matchMaterial((String)type)) == null && type.contains(":")) {
            material = Material.matchMaterial((String)type.substring(type.indexOf(58) + 1));
        }
        if (material == null) {
            notes.add("material '" + type + "' does not exist on this server");
            return null;
        }
        int amount = map.get("amount") instanceof Number ? ((Number)map.get("amount")).intValue() : 1;
        notes.add("rebuilt as plain " + material.name() + " (all meta lost)");
        ItemStack item = ItemStack.of(material);
        item.setAmount(Math.max(1, amount));
        return item;
    }

    private static Object salvageMeta(Map<String, Object> map, List<String> notes) {
        LinkedHashMap<String, Object> working = new LinkedHashMap<String, Object>(map);
        ArrayList<String> dropped = new ArrayList<String>();
        for (String[] step : META_STRIP_STEPS) {
            Object result;
            boolean removedAny = false;
            for (String key : step) {
                if (working.remove(key) == null) continue;
                dropped.add(key);
                removedAny = true;
            }
            if (!removedAny || (result = RepairManager.tryMeta(working)) == null) continue;
            notes.add("dropped " + String.join((CharSequence)", ", dropped));
            return result;
        }
        LinkedHashMap<String, Object> minimal = new LinkedHashMap<String, Object>();
        for (Map.Entry entry : working.entrySet()) {
            if (!META_SAFE_KEYS.contains(entry.getKey())) continue;
            minimal.put((String)entry.getKey(), entry.getValue());
        }
        Object result = RepairManager.tryMeta(minimal);
        if (result != null) {
            notes.add("kept only name and lore, dropped everything else");
            return result;
        }
        notes.add("item meta was unreadable and had to be dropped entirely");
        return META_DROPPED;
    }

    private static Object tryMeta(Map<String, Object> map) {
        try {
            ConfigurationSerializable result = ConfigurationSerialization.deserializeObject(map);
            return result instanceof ItemMeta ? result : null;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private String readRaw(String storageKey) throws Exception {
        if (this.plugin.getStorageTypeName().equalsIgnoreCase("mysql")) {
            try (Connection conn = this.plugin.getSQLConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT data FROM " + this.plugin.getTableName() + " WHERE uuid=?")) {
                ps.setString(1, storageKey);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("data") : null;
                }
            }
        }
        File file = new File(this.plugin.getChestDataFolder(), storageKey + ".data");
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        }
    }

    private void writeRaw(String storageKey, String base64) throws Exception {
        if (this.plugin.getStorageTypeName().equalsIgnoreCase("mysql")) {
            try (Connection conn = this.plugin.getSQLConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE " + this.plugin.getTableName() + " SET data=? WHERE uuid=?");){
                ps.setString(1, base64);
                ps.setString(2, storageKey);
                ps.executeUpdate();
            }
            return;
        }
        File file = new File(this.plugin.getChestDataFolder(), storageKey + ".data");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));){
            writer.write(base64);
        }
    }

    private File snapshot(String storageKey, String base64) throws Exception {
        File folder = new File(this.plugin.getDataFolder(), "repair-backups");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IOException("could not create " + folder.getPath());
        }
        File out = new File(folder, storageKey + "_" + LocalDateTime.now().format(STAMP) + ".data");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(out));){
            writer.write(base64 == null ? "" : base64);
        }
        return out;
    }

    private TreeSet<Integer> storedPages(UUID ownerId) {
        TreeSet<Integer> pages = new TreeSet<Integer>();
        String uuid = ownerId.toString();
        if (this.plugin.getStorageTypeName().equalsIgnoreCase("mysql")) {
            try (Connection conn = this.plugin.getSQLConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM " + this.plugin.getTableName() + " WHERE uuid=? OR uuid LIKE ? ESCAPE '!'");){
                ps.setString(1, uuid);
                ps.setString(2, uuid + "!_page%");
                try (ResultSet rs = ps.executeQuery();){
                    while (rs.next()) {
                        RepairManager.addPage(pages, rs.getString("uuid"), uuid);
                    }
                }
            }
            catch (Exception e) {
                this.plugin.getLogger().warning("[Repair] Could not list stored pages: " + e.getMessage());
            }
        } else {
            File[] files = this.plugin.getChestDataFolder().listFiles((dir, name) -> name.startsWith(uuid) && name.endsWith(".data"));
            if (files != null) {
                for (File file : files) {
                    String name2 = file.getName();
                    RepairManager.addPage(pages, name2.substring(0, name2.length() - ".data".length()), uuid);
                }
            }
        }
        if (pages.isEmpty()) {
            pages.add(1);
        }
        return pages;
    }

    private static void addPage(TreeSet<Integer> pages, String storageKey, String uuid) {
        if (storageKey.equals(uuid)) {
            pages.add(1);
            return;
        }
        String prefix = uuid + "_page";
        if (!storageKey.startsWith(prefix)) {
            return;
        }
        try {
            pages.add(Integer.parseInt(storageKey.substring(prefix.length())));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("help") || args[1].equals("?")) {
            this.sendUsage(sender);
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("scan")) {
            int listPage = 1;
            if (args.length >= 3) {
                Integer parsed = this.parsePositive(sender, args[2]);
                if (parsed == null) {
                    return;
                }
                listPage = parsed;
            }
            this.scan(sender, listPage);
            return;
        }
        if (action.equals("undo")) {
            if (args.length < 3 || args.length > 4) {
                this.send(sender, RepairManager.legacy(this.plugin.prefixed("&cUsage: &f/ec repair undo <player> [page]")));
                return;
            }
            Integer page = null;
            if (args.length == 4 && (page = this.parsePositive(sender, args[3])) == null) {
                return;
            }
            Integer chestPage = page;
            this.plugin.resolveTargetPlayer(sender, args[2], (uuid, name) -> this.undo(sender, (UUID)uuid, (String)name, chestPage));
            return;
        }
        Target target = RepairManager.parseTarget(args);
        if (target == null) {
            this.sendUsage(sender);
            return;
        }
        Integer page = null;
        if (target.rawPage != null && (page = this.parsePositive(sender, target.rawPage)) == null) {
            return;
        }
        Integer chestPage = page;
        this.plugin.resolveTargetPlayer(sender, target.playerName, (uuid, name) -> this.inspect(sender, (UUID)uuid, (String)name, chestPage, target.confirm));
    }

    static Target parseTarget(String[] args) {
        if (args.length < 2 || args.length > 4) {
            return null;
        }
        boolean confirm = args[args.length - 1].equalsIgnoreCase("confirm");
        int pageIndex = confirm ? args.length - 2 : args.length - 1;
        return new Target(args[1], pageIndex >= 2 ? args[pageIndex] : null, confirm);
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        ArrayList<String> options = new ArrayList<String>();
        if (args.length == 2) {
            options.add("scan");
            options.add("undo");
            for (Player online : Bukkit.getOnlinePlayers()) {
                options.add(online.getName());
            }
        } else if (args[1].equalsIgnoreCase("undo") && args.length == 3) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                options.add(online.getName());
            }
        } else {
            if (args[1].equalsIgnoreCase("scan")) {
                return List.of();
            }
            if (args.length == 3) {
                if (this.plugin.arePagesEnabled()) {
                    for (int i = 1; i <= this.plugin.getMaxPagesSetting(); ++i) {
                        options.add(String.valueOf(i));
                    }
                }
                options.add("confirm");
            } else if (args.length == 4) {
                options.add("confirm");
            }
        }
        String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
        ArrayList<String> matches = new ArrayList<String>();
        for (String option : options) {
            if (!option.toLowerCase(Locale.ROOT).startsWith(typed)) continue;
            matches.add(option);
        }
        return matches;
    }

    private Integer parsePositive(CommandSender sender, String value) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed >= 1) {
                return parsed;
            }
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        this.send(sender, RepairManager.legacy(this.plugin.prefixed("&cPage must be a number of 1 or higher, not &f" + value + "&c.")));
        return null;
    }

    private void sendUsage(CommandSender sender) {
        ArrayList<Component> out = new ArrayList<Component>();
        out.add(RepairManager.header());
        out.add(RepairManager.legacy("\u00a77Recovers chests that refuse to open because a single stored item can no"));
        out.add(RepairManager.legacy("\u00a77longer be read - the other items are never lost, only hidden behind it."));
        out.add((Component)Component.empty());
        out.add(RepairManager.usageLine("/ec repair scan", "List every chest that cannot be read"));
        out.add(RepairManager.usageLine("/ec repair <player> [page]", "Report what a repair would do - changes nothing"));
        out.add(RepairManager.usageLine("/ec repair <player> [page] confirm", "Apply the repair"));
        out.add(RepairManager.usageLine("/ec repair undo <player> [page]", "Put the pre-repair data back"));
        out.add((Component)Component.empty());
        out.add(RepairManager.legacy("\u00a78Originals are copied to plugins/EnderChest/repair-backups/ before any write."));
        out.add(RepairManager.button("[ Scan now ]", "/ec repair scan", "Look for damaged chests"));
        this.send(sender, out);
    }

    private void inspect(CommandSender sender, UUID ownerId, String ownerName, Integer onlyPage, boolean confirm) {
        if (this.plugin.isChestBusy(ownerId)) {
            this.send(sender, RepairManager.legacy(this.plugin.prefixed("&c" + ownerName + "'s EnderChest is open or saving right now. Wait a moment and try again.")));
            return;
        }
        this.plugin.getFoliaLib().getScheduler().runAsync(task -> {
            ArrayList<Component> out = new ArrayList<Component>();
            List<PageReport> reports = this.readPages(ownerId, onlyPage, out);
            if (reports.isEmpty()) {
                out.add(RepairManager.legacy(this.plugin.prefixed("&eNo stored EnderChest data found for &f" + ownerName + (String)(onlyPage == null ? "" : "&e page &f" + onlyPage) + "&e.")));
                this.send(sender, out);
                return;
            }
            int readable = 0;
            int reduced = 0;
            int lost = 0;
            boolean truncated = false;
            ArrayList<Integer> okPages = new ArrayList<Integer>();
            ArrayList<PageReport> broken = new ArrayList<PageReport>();
            for (PageReport report : reports) {
                readable += report.recoveredItems;
                if (!report.isBroken()) {
                    okPages.add(report.page);
                    continue;
                }
                broken.add(report);
                for (SlotReport slot : report.problems) {
                    if (slot.outcome == Outcome.REPAIRED) {
                        ++reduced;
                        continue;
                    }
                    if (slot.outcome == Outcome.LOST) {
                        ++lost;
                        continue;
                    }
                    truncated = true;
                }
            }
            out.add(RepairManager.header());
            out.add(RepairManager.legacy("\u00a77Player: \u00a7f" + ownerName + " \u00a78\u00b7 \u00a77" + (onlyPage == null ? reports.size() + " page(s) stored" : "page " + onlyPage + " only")));
            if (broken.isEmpty()) {
                out.add(RepairManager.legacy("\u00a7a\u2714 " + (String)(onlyPage == null ? "Everything is" : "Page " + onlyPage + " is") + " readable \u00a78(" + readable + " items) \u00a77- nothing to repair."));
                this.send(sender, out);
                return;
            }
            for (PageReport report : broken) {
                if (report.fatalError != null) {
                    out.add(RepairManager.hover(RepairManager.legacy("\u00a77Page \u00a7f" + report.page + " \u00a78- \u00a7cunreadable \u00a78(hover)"), "\u00a77" + report.fatalError));
                    this.plugin.getLogger().warning("[Repair] " + ownerName + " page " + report.page + ": unreadable - " + report.fatalError);
                    continue;
                }
                out.add(RepairManager.legacy("\u00a77Page \u00a7f" + report.page + " \u00a78- \u00a7e" + report.problems.size() + " damaged slot(s) \u00a78(" + report.recoveredItems + " of " + report.declaredSlots + " slots recovered)"));
                for (int i = 0; i < report.problems.size(); ++i) {
                    if (i == 8) {
                        out.add(RepairManager.legacy("\u00a78   \u2026 and " + (report.problems.size() - 8) + " more - full list in the console"));
                        break;
                    }
                    out.add(this.slotLine(report.problems.get(i)));
                }
                for (SlotReport slot : report.problems) {
                    this.plugin.getLogger().info("[Repair] " + ownerName + " page " + report.page + ": " + slot.describe());
                }
            }
            if (!okPages.isEmpty()) {
                out.add(RepairManager.legacy("\u00a78Readable pages: " + RepairManager.join(okPages)));
            }
            out.add((Component)Component.empty());
            out.add(RepairManager.legacy("\u00a77Recoverable: \u00a7a" + readable + " items\u00a77 \u00b7 reduced: \u00a7e" + reduced + "\u00a77 \u00b7 unrecoverable: \u00a7c" + lost));
            if (truncated) {
                out.add(RepairManager.legacy("\u00a7cThe stored data breaks off part-way, so anything after that point"));
                out.add(RepairManager.legacy("\u00a7ccannot be recovered - a repair keeps only what is listed above."));
            }
            if (!confirm) {
                out.add(RepairManager.legacy("\u00a77Nothing has been changed."));
                out.add(RepairManager.button("[ Apply repair ]", this.confirmCommand(ownerName, onlyPage), "Writes the recovered items back"));
                out.add(RepairManager.legacy("\u00a78The untouched original is copied to repair-backups/ first."));
                this.send(sender, out);
                return;
            }
            int written = 0;
            for (PageReport report : broken) {
                if (!report.canWriteBack()) {
                    out.add(RepairManager.legacy("\u00a7cPage " + report.page + " could not be rebuilt at all and was left untouched."));
                    continue;
                }
                try {
                    File backup = this.snapshot(report.storageKey, report.originalBase64);
                    this.writeRaw(report.storageKey, EnderChest.itemStackArrayToBase64(report.items));
                    ++written;
                    this.plugin.getLogger().info("[Repair] Rewrote " + report.storageKey + " for " + ownerName + " (" + report.recoveredItems + " items kept); original saved as " + backup.getName());
                }
                catch (Exception e) {
                    out.add(RepairManager.legacy("\u00a7cFailed to write page " + report.page + ": " + RepairManager.rootMessage(e)));
                    this.plugin.getLogger().warning("[Repair] Write failed for " + report.storageKey + ": " + RepairManager.rootMessage(e));
                }
            }
            this.plugin.clearLoadFailure(ownerId);
            out.add(RepairManager.legacy("\u00a7a\u2714 Repaired \u00a7f" + written + "\u00a7a page(s) - \u00a7f" + ownerName + "\u00a7a can open the EnderChest again."));
            if (written > 0) {
                out.add(RepairManager.button("[ Undo ]", "/ec repair undo " + ownerName + (String)(onlyPage == null ? "" : " " + onlyPage), "Restores the data from before this repair"));
            }
            this.send(sender, out);
        });
    }

    private String confirmCommand(String ownerName, Integer page) {
        return "/ec repair " + ownerName + (String)(page == null ? "" : " " + page) + " confirm";
    }

    private Component slotLine(SlotReport slot) {
        String verdict = slot.outcome == Outcome.REPAIRED ? "\u00a7ekept, details reduced" : (slot.outcome == Outcome.LOST ? "\u00a7cnot recoverable, slot emptied" : "\u00a7cstored data ends here");
        String where = slot.outcome == Outcome.TRUNCATED ? "slot \u00a7f" + slot.slot + "+" : "slot \u00a7f" + slot.slot;
        return RepairManager.hover(RepairManager.legacy("\u00a78 \u2022 \u00a77" + where + " \u00a78- \u00a7f" + (slot.item == null ? "unknown item" : slot.item) + " \u00a78\u2192 " + verdict + " \u00a78(hover)"), "\u00a77" + slot.detail);
    }

    private List<PageReport> readPages(UUID ownerId, Integer onlyPage, List<Component> out) {
        ArrayList<PageReport> reports = new ArrayList<PageReport>();
        for (int page : this.storedPages(ownerId)) {
            if (onlyPage != null && onlyPage != page) continue;
            String key = this.plugin.getPageStorageKey(ownerId, page);
            try {
                String raw = this.readRaw(key);
                if (raw == null) continue;
                reports.add(RepairManager.read(raw, page, key));
            }
            catch (Exception e) {
                out.add(RepairManager.legacy("\u00a7cCould not read page " + page + ": " + RepairManager.rootMessage(e)));
            }
        }
        return reports;
    }

    private void scan(CommandSender sender, int listPage) {
        this.plugin.getFoliaLib().getScheduler().runAsync(task -> {
            int checked;
            ArrayList<Damaged> damaged;
            ArrayList<Component> out;
            block32: {
                out = new ArrayList<Component>();
                damaged = new ArrayList<Damaged>();
                if (this.plugin.getStorageTypeName().equalsIgnoreCase("mysql")) {
                    try (Connection conn = this.plugin.getSQLConnection();
                         PreparedStatement ps = conn.prepareStatement("SELECT uuid, data FROM " + this.plugin.getTableName());
                         ResultSet rs = ps.executeQuery();){
                        int seen = 0;
                        while (rs.next()) {
                            ++seen;
                            this.collectDamaged(damaged, rs.getString("uuid"), rs.getString("data"));
                        }
                        checked = seen;
                        break block32;
                    }
                    catch (Exception e) {
                        this.send(sender, RepairManager.legacy(this.plugin.prefixed("&cScan failed: " + RepairManager.rootMessage(e))));
                        return;
                    }
                }
                File[] files = this.plugin.getChestDataFolder().listFiles((dir, name) -> name.endsWith(".data"));
                int n = checked = files == null ? 0 : files.length;
                if (files != null) {
                    for (File file : files) {
                        String key = file.getName().substring(0, file.getName().length() - ".data".length());
                        try {
                            this.collectDamaged(damaged, key, this.readRaw(key));
                        }
                        catch (Exception e) {
                            damaged.add(new Damaged(key, false, key, RepairManager.pageOf(key), RepairManager.rootMessage(e)));
                        }
                    }
                }
            }
            out.add(RepairManager.header());
            if (damaged.isEmpty()) {
                out.add(RepairManager.legacy("\u00a7a\u2714 Scanned \u00a7f" + checked + "\u00a7a stored page(s) - all readable."));
                this.send(sender, out);
                return;
            }
            damaged.sort(Comparator.comparing((Damaged d) -> d.ownerName.toLowerCase(Locale.ROOT)).thenComparingInt(d -> d.page));
            int totalListPages = (damaged.size() + 8 - 1) / 8;
            int current = Math.min(listPage, totalListPages);
            int from = (current - 1) * 8;
            int to = Math.min(damaged.size(), from + 8);
            out.add(RepairManager.legacy("\u00a77Scanned \u00a7f" + checked + "\u00a77 stored page(s) \u00b7 \u00a7c" + damaged.size() + "\u00a77 damaged \u00a78(page " + current + "/" + totalListPages + ")"));
            for (Damaged entry : damaged.subList(from, to)) {
                Component row = RepairManager.legacy("\u00a78 \u2022 \u00a7f" + entry.ownerName + " \u00a78- \u00a77page \u00a7f" + entry.page + " \u00a78- \u00a7c" + entry.summary + " ");
                out.add((Component)(entry.nameKnown ? ((TextComponent.Builder)((TextComponent.Builder)Component.text().append(row)).append(RepairManager.button("[ Inspect ]", "/ec repair " + entry.ownerName, "Reports what a repair would do"))).build() : row));
            }
            for (Damaged entry : damaged) {
                this.plugin.getLogger().info("[Repair] Damaged: " + entry.ownerName + " (" + entry.storageKey + ") - " + entry.summary);
            }
            if (totalListPages > 1) {
                TextComponent.Builder nav = Component.text();
                if (current > 1) {
                    nav.append(RepairManager.button("[ \u00ab ]", "/ec repair scan " + (current - 1), "Previous page"));
                    nav.append(RepairManager.legacy(" "));
                }
                if (current < totalListPages) {
                    nav.append(RepairManager.button("[ \u00bb ]", "/ec repair scan " + (current + 1), "Next page"));
                }
                out.add((Component)nav.build());
            }
            out.add(RepairManager.legacy("\u00a78The complete list is in the console."));
            this.send(sender, out);
        });
    }

    private void collectDamaged(List<Damaged> damaged, String storageKey, String base64) {
        PageReport report = RepairManager.read(base64, RepairManager.pageOf(storageKey), storageKey);
        if (!report.isBroken()) {
            return;
        }
        String summary = report.fatalError != null ? "unreadable" : report.problems.size() + " damaged slot(s)";
        String name = this.ownerNameFor(storageKey);
        damaged.add(new Damaged(name, !name.equals(RepairManager.uuidPartOf(storageKey)), storageKey, RepairManager.pageOf(storageKey), summary));
    }

    private void undo(CommandSender sender, UUID ownerId, String ownerName, Integer onlyPage) {
        if (this.plugin.isChestBusy(ownerId)) {
            this.send(sender, RepairManager.legacy(this.plugin.prefixed("&c" + ownerName + "'s EnderChest is open or saving right now. Wait a moment and try again.")));
            return;
        }
        this.plugin.getFoliaLib().getScheduler().runAsync(task -> {
            ArrayList<Component> out = new ArrayList<Component>();
            Map<String, File> snapshots = this.newestSnapshots(ownerId, onlyPage);
            if (snapshots.isEmpty()) {
                out.add(RepairManager.legacy(this.plugin.prefixed("&eNo pre-repair copy found for &f" + ownerName + (String)(onlyPage == null ? "" : "&e page &f" + onlyPage) + "&e.")));
                this.send(sender, out);
                return;
            }
            out.add(RepairManager.header());
            int restored = 0;
            for (Map.Entry<String, File> entry : snapshots.entrySet()) {
                String key = entry.getKey();
                try {
                    String stored;
                    try (BufferedReader reader = new BufferedReader(new FileReader(entry.getValue()));){
                        stored = reader.readLine();
                    }
                    if (stored == null) {
                        out.add(RepairManager.legacy("\u00a7cThe copy for page " + RepairManager.pageOf(key) + " is empty and was skipped."));
                        continue;
                    }
                    this.snapshot(key, this.readRaw(key));
                    this.writeRaw(key, stored);
                    ++restored;
                    this.plugin.getLogger().info("[Repair] Restored " + key + " for " + ownerName + " from " + entry.getValue().getName());
                }
                catch (Exception e) {
                    out.add(RepairManager.legacy("\u00a7cCould not restore page " + RepairManager.pageOf(key) + ": " + RepairManager.rootMessage(e)));
                    this.plugin.getLogger().warning("[Repair] Undo failed for " + key + ": " + RepairManager.rootMessage(e));
                }
            }
            this.plugin.clearLoadFailure(ownerId);
            out.add(RepairManager.legacy("\u00a7a\u2714 Restored \u00a7f" + restored + "\u00a7a page(s) for \u00a7f" + ownerName + "\u00a7a from the pre-repair copy."));
            out.add(RepairManager.legacy("\u00a78That data may again contain the item that stopped the chest from opening."));
            this.send(sender, out);
        });
    }

    private Map<String, File> newestSnapshots(UUID ownerId, Integer onlyPage) {
        LinkedHashMap<String, File> newest = new LinkedHashMap<String, File>();
        LinkedHashMap<String, String> newestStamp = new LinkedHashMap<String, String>();
        File folder = new File(this.plugin.getDataFolder(), "repair-backups");
        File[] files = folder.listFiles((dir, name) -> name.startsWith(ownerId.toString()) && name.endsWith(".data"));
        if (files == null) {
            return newest;
        }
        for (File file : files) {
            String key;
            Matcher matcher = SNAPSHOT_NAME.matcher(file.getName());
            if (!matcher.matches() || !(key = matcher.group(1)).equals(ownerId.toString()) && !key.startsWith(String.valueOf(ownerId) + "_page") || onlyPage != null && onlyPage != RepairManager.pageOf(key)) continue;
            String stamp = matcher.group(2);
            if (newestStamp.containsKey(key) && stamp.compareTo((String)newestStamp.get(key)) <= 0) continue;
            newestStamp.put(key, stamp);
            newest.put(key, file);
        }
        return newest;
    }

    private static Component legacy(String text) {
        return Messages.parseCompatible(text);
    }

    private static Component hover(Component component, String tooltip) {
        return component.hoverEvent((HoverEventSource)HoverEvent.showText((Component)RepairManager.legacy(tooltip)));
    }

    private static Component button(String label, String command, String tooltip) {
        return RepairManager.legacy("\u00a7a\u00a7l" + label).clickEvent(ClickEvent.runCommand((String)command)).hoverEvent((HoverEventSource)HoverEvent.showText((Component)RepairManager.legacy("\u00a77" + tooltip + "\n\u00a78" + command)));
    }

    private static Component usageLine(String command, String description) {
        return ((TextComponent.Builder)((TextComponent.Builder)Component.text().append(RepairManager.hover(RepairManager.legacy("\u00a7d" + command), "\u00a77Click to insert\n\u00a78" + command).clickEvent(ClickEvent.suggestCommand((String)command)))).append(RepairManager.legacy(" \u00a78- \u00a77" + description))).build();
    }

    private static Component header() {
        return RepairManager.legacy("\u00a75\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501 \u00a7d\u00a7lEnderChest Repair \u00a75\u00a7l\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501");
    }

    private static String join(List<Integer> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); ++i) {
            builder.append(i == 0 ? "" : ", ").append(values.get(i));
        }
        return builder.toString();
    }

    private static String uuidPartOf(String storageKey) {
        int index = storageKey.indexOf("_page");
        return index < 0 ? storageKey : storageKey.substring(0, index);
    }

    private static int pageOf(String storageKey) {
        int index = storageKey.indexOf("_page");
        if (index < 0) {
            return 1;
        }
        try {
            return Integer.parseInt(storageKey.substring(index + "_page".length()));
        }
        catch (NumberFormatException e) {
            return 1;
        }
    }

    private String ownerNameFor(String storageKey) {
        String uuidPart = RepairManager.uuidPartOf(storageKey);
        try {
            String name = Bukkit.getOfflinePlayer((UUID)UUID.fromString(uuidPart)).getName();
            return name == null ? uuidPart : name;
        }
        catch (IllegalArgumentException e) {
            return uuidPart;
        }
    }

    private void send(CommandSender sender, Component line) {
        this.send(sender, List.of(line));
    }

    private void send(CommandSender sender, List<Component> lines) {
        if (lines.isEmpty()) {
            return;
        }
        ArrayList<Component> copy = new ArrayList<Component>(lines);
        this.plugin.getFoliaLib().getScheduler().runNextTick(task -> {
            for (Component line : copy) {
                sender.sendMessage(line);
            }
        });
    }

    public static final class PageReport {
        final int page;
        final String storageKey;
        final String originalBase64;
        final ItemStack[] items;
        final List<SlotReport> problems = new ArrayList<SlotReport>();
        String fatalError;
        int declaredSlots;
        int recoveredItems;

        PageReport(int page, String storageKey, String originalBase64, ItemStack[] items) {
            this.page = page;
            this.storageKey = storageKey;
            this.originalBase64 = originalBase64;
            this.items = items;
        }

        public boolean isBroken() {
            return this.fatalError != null || !this.problems.isEmpty();
        }

        public boolean canWriteBack() {
            return this.fatalError == null && this.items != null;
        }
    }

    private static final class SalvagingInputStream
    extends ObjectInputStream {
        private static Field wrapperMapField;
        final List<String> notes = new ArrayList<String>();
        String lastItemName;

        SalvagingInputStream(InputStream in) throws IOException {
            super(in);
            this.enableResolveObject(true);
        }

        @Override
        protected Object resolveObject(Object obj) {
            if (obj == null || !RepairManager.WRAPPER_CLASS.equals(obj.getClass().getName())) {
                return obj;
            }
            Map<String, Object> map = this.unwrap(obj);
            if (map == null) {
                this.notes.add("stored data could not be unwrapped");
                return RepairManager.META_DROPPED;
            }
            if (map.containsKey("type")) {
                this.lastItemName = String.valueOf(map.get("type"));
            }
            try {
                ConfigurationSerializable result = ConfigurationSerialization.deserializeObject(map);
                if (result != null) {
                    return result;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return RepairManager.salvage(map, this.notes);
        }

        private Map<String, Object> unwrap(Object wrapper) {
            try {
                Object map;
                if (wrapperMapField == null) {
                    Field field = wrapper.getClass().getDeclaredField("map");
                    field.setAccessible(true);
                    wrapperMapField = field;
                }
                return (map = wrapperMapField.get(wrapper)) instanceof Map ? new LinkedHashMap((Map)map) : null;
            }
            catch (Exception e) {
                return null;
            }
        }
    }

    public static final class SlotReport {
        final int slot;
        final Outcome outcome;
        final String item;
        final String detail;

        SlotReport(int slot, Outcome outcome, String item, String detail) {
            this.slot = slot;
            this.outcome = outcome;
            this.item = item;
            this.detail = detail;
        }

        public String describe() {
            if (this.outcome == Outcome.TRUNCATED) {
                return "slot " + this.slot + " onwards: data ends here - " + this.detail;
            }
            String prefix = "slot " + this.slot + " (" + (this.item == null ? "unknown item" : this.item) + "): ";
            return this.outcome == Outcome.REPAIRED ? prefix + "recovered, " + this.detail : prefix + "not recoverable, slot emptied - " + this.detail;
        }
    }

    public static enum Outcome {
        REPAIRED,
        LOST,
        TRUNCATED;

    }

    static final class Target {
        final String playerName;
        final String rawPage;
        final boolean confirm;

        Target(String playerName, String rawPage, boolean confirm) {
            this.playerName = playerName;
            this.rawPage = rawPage;
            this.confirm = confirm;
        }
    }

    private static final class Damaged {
        final String ownerName;
        final boolean nameKnown;
        final String storageKey;
        final int page;
        final String summary;

        Damaged(String ownerName, boolean nameKnown, String storageKey, int page, String summary) {
            this.ownerName = ownerName;
            this.nameKnown = nameKnown;
            this.storageKey = storageKey;
            this.page = page;
            this.summary = summary;
        }
    }
}
