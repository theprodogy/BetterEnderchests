/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 */
package com.fernsehheft.enderchest.nbtapi.utils.metrics.bukkit;

import com.fernsehheft.enderchest.nbtapi.utils.metrics.MetricsBase;
import com.fernsehheft.enderchest.nbtapi.utils.metrics.charts.CustomChart;
import com.fernsehheft.enderchest.nbtapi.utils.metrics.json.JsonObjectBuilder;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Metrics {
    private final Plugin plugin;
    private final MetricsBase metricsBase;

    public Metrics(Plugin plugin, int serviceId) {
        this.plugin = plugin;
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)configFile);
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", (Object)true);
            config.addDefault("serverUuid", (Object)UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", (Object)false);
            config.addDefault("logSentData", (Object)false);
            config.addDefault("logResponseStatusText", (Object)false);
            config.options().header("bStats (https://bStats.org) collects some basic information for plugin authors, like how\nmany people use their plugin and their total player count. It's recommended to keep bStats\nenabled, but if you're not comfortable with this, you can turn this setting off. There is no\nperformance penalty associated with having metrics enabled, and data sent to bStats is fully\nanonymous.").copyDefaults(true);
            try {
                config.save(configFile);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        boolean enabled = config.getBoolean("enabled", true);
        String serverUUID = config.getString("serverUuid");
        boolean logErrors = config.getBoolean("logFailedRequests", false);
        boolean logSentData = config.getBoolean("logSentData", false);
        boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        boolean isFolia = false;
        try {
            isFolia = Class.forName("io.papermc.paper.threadedregions.RegionizedServer") != null;
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.metricsBase = new MetricsBase("bukkit", serverUUID, serviceId, enabled, this::appendPlatformData, this::appendServiceData, isFolia ? null : submitDataTask -> Bukkit.getScheduler().runTask(plugin, submitDataTask), () -> ((Plugin)plugin).isEnabled(), (message, error) -> this.plugin.getLogger().log(Level.WARNING, (String)message, (Throwable)error), message -> this.plugin.getLogger().log(Level.INFO, (String)message), logErrors, logSentData, logResponseStatusText, false);
    }

    public void shutdown() {
        this.metricsBase.shutdown();
    }

    public void addCustomChart(CustomChart chart) {
        this.metricsBase.addCustomChart(chart);
    }

    private void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("playerAmount", this.getPlayerAmount());
        builder.appendField("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
        builder.appendField("bukkitVersion", Bukkit.getVersion());
        builder.appendField("bukkitName", Bukkit.getName());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", this.plugin.getDescription().getVersion());
    }

    private int getPlayerAmount() {
        try {
            Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers", new Class[0]);
            return onlinePlayersMethod.getReturnType().equals(Collection.class) ? ((Collection)onlinePlayersMethod.invoke((Object)Bukkit.getServer(), new Object[0])).size() : ((Player[])onlinePlayersMethod.invoke((Object)Bukkit.getServer(), new Object[0])).length;
        }
        catch (Exception e) {
            return Bukkit.getOnlinePlayers().size();
        }
    }
}

