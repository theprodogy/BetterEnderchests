/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.fernsehheft.enderchest.nbtapi.utils;

import com.fernsehheft.enderchest.nbtapi.utils.VersionChecker;
import com.fernsehheft.enderchest.nbtapi.utils.metrics.bukkit.Metrics;
import com.fernsehheft.enderchest.nbtapi.utils.metrics.charts.DrilldownPie;
import com.fernsehheft.enderchest.nbtapi.utils.metrics.charts.SimplePie;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ClassWrapper;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public enum MinecraftVersion {
    UNKNOWN(Integer.MAX_VALUE),
    MC1_7_R4(174),
    MC1_8_R3(183),
    MC1_9_R1(191),
    MC1_9_R2(192),
    MC1_10_R1(1101),
    MC1_11_R1(1111),
    MC1_12_R1(1121),
    MC1_13_R1(1131),
    MC1_13_R2(1132),
    MC1_14_R1(1141),
    MC1_15_R1(1151),
    MC1_16_R1(1161),
    MC1_16_R2(1162),
    MC1_16_R3(1163),
    MC1_17_R1(1171),
    MC1_18_R1(1181, true),
    MC1_18_R2(1182, true),
    MC1_19_R1(1191, true),
    MC1_19_R2(1192, true),
    MC1_19_R3(1193, true),
    MC1_20_R1(1201, true),
    MC1_20_R2(1202, true),
    MC1_20_R3(1203, true),
    MC1_20_R4(1204, true),
    MC1_21_R1(1211, true),
    MC1_21_R2(1212, true),
    MC1_21_R3(1213, true),
    MC1_21_R4(1214, true),
    MC1_21_R5(1215, true),
    MC1_21_R6(1216, true),
    MC1_21_R7(1217, true);

    private static MinecraftVersion version;
    private static Boolean hasGsonSupport;
    private static Boolean isForgePresent;
    private static Boolean isNeoForgePresent;
    private static Boolean isFabricPresent;
    private static Boolean isFoliaPresent;
    private static boolean bStatsDisabled;
    private static boolean disablePackageWarning;
    private static boolean updateCheckDisabled;
    private static Logger logger;
    protected static final String VERSION = "2.15.5";
    private final int versionId;
    private final boolean mojangMapping;
    private static final Map<String, MinecraftVersion> VERSION_TO_REVISION;

    private MinecraftVersion(int versionId) {
        this(versionId, false);
    }

    private MinecraftVersion(int versionId, boolean mojangMapping) {
        this.versionId = versionId;
        this.mojangMapping = mojangMapping;
    }

    public int getVersionId() {
        return this.versionId;
    }

    public boolean isMojangMapping() {
        return this.mojangMapping;
    }

    public String getPackageName() {
        if (this == UNKNOWN) {
            try {
                return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return this.name().replace("MC", "v");
    }

    public static boolean isAtLeastVersion(MinecraftVersion version) {
        return MinecraftVersion.getVersion().getVersionId() >= version.getVersionId();
    }

    public static boolean isNewerThan(MinecraftVersion version) {
        return MinecraftVersion.getVersion().getVersionId() > version.getVersionId();
    }

    public static MinecraftVersion getVersion() {
        if (version != null) {
            return version;
        }
        try {
            String ver = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            logger.info("[NBTAPI] Found Minecraft: " + ver + "! Trying to find NMS support");
            version = MinecraftVersion.valueOf(ver.replace("v", "MC"));
        }
        catch (Exception ex) {
            logger.info("[NBTAPI] Found Minecraft: " + Bukkit.getServer().getBukkitVersion().split("-")[0] + "! Trying to find NMS support");
            version = VERSION_TO_REVISION.getOrDefault(Bukkit.getServer().getBukkitVersion().split("-")[0], UNKNOWN);
        }
        if (version != UNKNOWN) {
            logger.info("[NBTAPI] NMS support '" + version.name() + "' loaded!");
        } else {
            logger.warning("[NBTAPI] This Server-Version(" + Bukkit.getServer().getBukkitVersion() + ") is not supported by this NBT-API Version(" + VERSION + ") located in " + VersionChecker.getPlugin() + ". The NBT-API will try to work as good as it can! Some functions may not work!");
        }
        MinecraftVersion.init();
        return version;
    }

    public static String getNBTAPIVersion() {
        return VERSION;
    }

    private static void init() {
        String defaultPackage = new String(new byte[]{100, 101, 46, 116, 114, 55, 122, 119, 46, 99, 104, 97, 110, 103, 101, 109, 101, 46, 110, 98, 116, 97, 112, 105, 46, 117, 116, 105, 108, 115});
        String reservedPackage = new String(new byte[]{100, 101, 46, 116, 114, 55, 122, 119, 46, 110, 98, 116, 97, 112, 105, 46, 117, 116, 105, 108, 115});
        try {
            if (MinecraftVersion.hasGsonSupport() && !bStatsDisabled) {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(VersionChecker.getPlugin());
                if (plugin != null && plugin instanceof JavaPlugin) {
                    MinecraftVersion.getLogger().info("[NBTAPI] Using the plugin '" + plugin.getName() + "' to create a bStats instance!");
                    Metrics metrics = new Metrics((Plugin)((JavaPlugin)plugin), 1058);
                    metrics.addCustomChart(new SimplePie("nbtapi_version", () -> VERSION));
                    metrics.addCustomChart(new DrilldownPie("nms_version", () -> {
                        HashMap map = new HashMap();
                        HashMap<String, Integer> entry = new HashMap<String, Integer>();
                        entry.put(Bukkit.getName(), 1);
                        map.put(MinecraftVersion.getVersion().name(), entry);
                        return map;
                    }));
                    metrics.addCustomChart(new SimplePie("shaded", () -> Boolean.toString(!"NBTAPI".equals(VersionChecker.getPlugin()))));
                    metrics.addCustomChart(new SimplePie("server_software", () -> Bukkit.getName()));
                    metrics.addCustomChart(new SimplePie("parent_plugin", () -> VersionChecker.getPluginforBStats()));
                    metrics.addCustomChart(new SimplePie("parent_plugin_type", () -> VersionChecker.getPluginType()));
                    metrics.addCustomChart(new SimplePie("special_environment", () -> {
                        if (MinecraftVersion.isFoliaPresent()) {
                            return "Folia";
                        }
                        if (MinecraftVersion.isForgePresent()) {
                            return "Forge";
                        }
                        if (MinecraftVersion.isFabricPresent()) {
                            return "Fabric";
                        }
                        if (MinecraftVersion.isNeoForgePresent()) {
                            return "NeoForge";
                        }
                        return "None";
                    }));
                    metrics.addCustomChart(new SimplePie("bindings_check", () -> {
                        boolean failedBinding = false;
                        for (ClassWrapper classWrapper : ClassWrapper.values()) {
                            if (!classWrapper.isEnabled() || classWrapper.getClazz() != null) continue;
                            failedBinding = true;
                        }
                        for (Enum enum_ : ReflectionMethod.values()) {
                            if (!((ReflectionMethod)enum_).isCompatible() || ((ReflectionMethod)enum_).isLoaded()) continue;
                            failedBinding = true;
                        }
                        return failedBinding ? "Failed" : "Pass";
                    }));
                } else if (plugin == null) {
                    MinecraftVersion.getLogger().info("[NBTAPI] Unable to create a bStats instance!!");
                }
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "[NBTAPI] Error enabling Metrics!", ex);
        }
        if (MinecraftVersion.hasGsonSupport() && !updateCheckDisabled) {
            new Thread(() -> {
                try {
                    VersionChecker.checkForUpdates();
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "[NBTAPI] Error while checking for updates! Error: " + ex.getMessage());
                }
            }).start();
        }
        if (!disablePackageWarning && MinecraftVersion.class.getPackage().getName().equals(defaultPackage)) {
            logger.warning("#########################################- NBTAPI -#########################################");
            logger.warning("The NBT-API package has not been moved! This *will* cause problems with other plugins containing");
            logger.warning("a different version of the api! Please read the guide on the plugin page on how to get the");
            logger.warning("Maven Shade plugin to relocate the api to your personal location! If you are not the developer,");
            logger.warning("please check your plugins and contact their developer, so they can fix this issue.");
            logger.warning("#########################################- NBTAPI -#########################################");
        }
        if (!disablePackageWarning && !"NBTAPI".equals(VersionChecker.getPlugin())) {
            if (!"de.tr7zw.nbtapi.utils".equals(reservedPackage)) {
                logger.warning("#########################################- NBTAPI -#########################################");
                logger.warning("The NBT-API inside " + VersionChecker.getPlugin() + " is the plugin version, not the API!");
                logger.warning("The plugin itself should never be shaded! Remove the `-plugin` from the dependency and fix your shading setup.");
                logger.warning("For more info check: https://github.com/tr7zw/Item-NBT-API/wiki/Using-Maven#option-2-shading-the-nbt-api-into-your-plugin");
                logger.warning("#########################################- NBTAPI -#########################################");
                return;
            }
            if (MinecraftVersion.class.getPackage().getName().equals("de.tr7zw.nbtapi.utils")) {
                logger.warning("#########################################- NBTAPI -#########################################");
                logger.warning("The NBT-API inside " + VersionChecker.getPlugin() + " is located at 'de.tr7zw.nbtapi.utils'!");
                logger.warning("This package name is reserved for the official NBTAPI plugin, and not intended to be used for shading!");
                logger.warning("Please change the relocate to something else. For example: com.example.util.nbtapi");
                logger.warning("#########################################- NBTAPI -#########################################");
            }
        }
    }

    public static boolean hasGsonSupport() {
        if (hasGsonSupport != null) {
            return hasGsonSupport;
        }
        try {
            Class.forName("com.google.gson.Gson");
            hasGsonSupport = true;
        }
        catch (Exception ex) {
            logger.info("[NBTAPI] Gson not found! This will not allow the usage of some methods!");
            hasGsonSupport = false;
        }
        return hasGsonSupport;
    }

    public static boolean isFabricPresent() {
        if (isFabricPresent != null) {
            return isFabricPresent;
        }
        try {
            logger.info("[NBTAPI] Found Fabric: " + Class.forName("net.fabricmc.api.ModInitializer"));
            isFabricPresent = true;
        }
        catch (Exception ex) {
            isFabricPresent = false;
        }
        return isFabricPresent;
    }

    public static boolean isForgePresent() {
        if (isForgePresent != null) {
            return isForgePresent;
        }
        try {
            logger.info("[NBTAPI] Found Forge: " + (MinecraftVersion.getVersion() == MC1_7_R4 ? Class.forName("cpw.mods.fml.common.Loader") : Class.forName("net.minecraftforge.fml.common.Loader")));
            isForgePresent = true;
        }
        catch (Exception ex) {
            isForgePresent = false;
        }
        return isForgePresent;
    }

    public static boolean isNeoForgePresent() {
        if (isNeoForgePresent != null) {
            return isNeoForgePresent;
        }
        try {
            logger.info("[NBTAPI] Found NeoForge: " + Class.forName("net.neoforged.neoforge.common.NeoForge"));
            isNeoForgePresent = true;
        }
        catch (Exception ex) {
            isNeoForgePresent = false;
        }
        return isNeoForgePresent;
    }

    public static boolean isFoliaPresent() {
        if (isFoliaPresent != null) {
            return isFoliaPresent;
        }
        try {
            logger.info("[NBTAPI] Found Folia: " + Class.forName("io.papermc.paper.threadedregions.RegionizedServer"));
            isFoliaPresent = true;
        }
        catch (Exception ex) {
            isFoliaPresent = false;
        }
        return isFoliaPresent;
    }

    public static void disableBStats() {
        bStatsDisabled = true;
    }

    public static void disableUpdateCheck() {
        updateCheckDisabled = true;
    }

    public static void enableUpdateCheck() {
        updateCheckDisabled = false;
    }

    public static void disablePackageWarning() {
        disablePackageWarning = true;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void replaceLogger(Logger logger) {
        if (logger == null) {
            throw new NullPointerException("Logger can not be null!");
        }
        MinecraftVersion.logger = logger;
    }

    static {
        bStatsDisabled = false;
        disablePackageWarning = false;
        updateCheckDisabled = true;
        logger = Logger.getLogger("NBTAPI");
        VERSION_TO_REVISION = new HashMap<String, MinecraftVersion>(){
            {
                this.put("1.20", MC1_20_R1);
                this.put("1.20.1", MC1_20_R1);
                this.put("1.20.2", MC1_20_R2);
                this.put("1.20.3", MC1_20_R3);
                this.put("1.20.4", MC1_20_R3);
                this.put("1.20.5", MC1_20_R4);
                this.put("1.20.6", MC1_20_R4);
                this.put("1.21", MC1_21_R1);
                this.put("1.21.1", MC1_21_R1);
                this.put("1.21.2", MC1_21_R2);
                this.put("1.21.3", MC1_21_R2);
                this.put("1.21.4", MC1_21_R3);
                this.put("1.21.5", MC1_21_R4);
                this.put("1.21.6", MC1_21_R5);
                this.put("1.21.7", MC1_21_R5);
                this.put("1.21.8", MC1_21_R5);
                this.put("1.21.9", MC1_21_R6);
                this.put("1.21.10", MC1_21_R6);
                this.put("1.21.11", MC1_21_R7);
            }
        };
    }
}

