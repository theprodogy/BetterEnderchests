/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.fernsehheft.enderchest.nbtapi.utils;

import com.fernsehheft.enderchest.nbtapi.NBTItem;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

public class VersionChecker {
    private static final String USER_AGENT = "nbt-api Version check";
    private static final String REQUEST_URL = "https://api.spiget.org/v2/resources/7939/versions?size=100";
    public static boolean hideOk = false;

    protected static void checkForUpdates() throws Exception {
        URL url = new URL(REQUEST_URL);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        JsonElement element = new JsonParser().parse((Reader)reader);
        if (element.isJsonArray()) {
            JsonArray updates = (JsonArray)element;
            JsonObject latest = (JsonObject)updates.get(updates.size() - 1);
            int versionDifference = VersionChecker.getVersionDifference(latest.get("name").getAsString());
            if (versionDifference == -1) {
                MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] The NBT-API in '" + VersionChecker.getPlugin() + "' seems to be outdated!");
                MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Current Version: '2.15.5' Newest Version: " + latest.get("name").getAsString() + "'");
                MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Please update the NBTAPI or the plugin that contains the api(nag the mod author when the newest release has an old version, not the NBTAPI dev)!");
            } else if (versionDifference == 0) {
                if (!hideOk) {
                    MinecraftVersion.getLogger().log(Level.INFO, "[NBTAPI] The NBT-API seems to be up-to-date!");
                }
            } else if (versionDifference == 1) {
                MinecraftVersion.getLogger().log(Level.INFO, "[NBTAPI] The NBT-API in '" + VersionChecker.getPlugin() + "' seems to be a future Version, not yet released on Spigot/CurseForge! This is not an error!");
                MinecraftVersion.getLogger().log(Level.INFO, "[NBTAPI] Current Version: '2.15.5' Newest Version: " + latest.get("name").getAsString() + "'");
            }
        } else {
            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error when looking for Updates! Got non Json Array: '" + element.toString() + "'");
        }
    }

    private static int getVersionDifference(String version) {
        int relPatchN;
        String current = "2.15.5";
        if (current.equals(version)) {
            return 0;
        }
        String pattern = "\\.";
        if (current.split(pattern).length != 3 || version.split(pattern).length != 3) {
            return -1;
        }
        int curMaj = Integer.parseInt(current.split(pattern)[0]);
        int curMin = Integer.parseInt(current.split(pattern)[1]);
        String curPatch = current.split(pattern)[2];
        int relMaj = Integer.parseInt(version.split(pattern)[0]);
        int relMin = Integer.parseInt(version.split(pattern)[1]);
        String relPatch = version.split(pattern)[2];
        if (curMaj < relMaj) {
            return -1;
        }
        if (curMaj > relMaj) {
            return 1;
        }
        if (curMin < relMin) {
            return -1;
        }
        if (curMin > relMin) {
            return 1;
        }
        int curPatchN = Integer.parseInt(curPatch.split("-")[0]);
        if (curPatchN < (relPatchN = Integer.parseInt(relPatch.split("-")[0]))) {
            return -1;
        }
        if (curPatchN > relPatchN) {
            return 1;
        }
        if (!relPatch.contains("-") && curPatch.contains("-")) {
            return -1;
        }
        if (relPatch.contains("-") && curPatch.contains("-")) {
            return 0;
        }
        return 1;
    }

    protected static String getPlugin() {
        block19: {
            InputStream inputStream;
            ClassLoader classLoader;
            block18: {
                classLoader = VersionChecker.class.getClassLoader();
                inputStream = classLoader.getResourceAsStream("paper-plugin.yml");
                if (inputStream != null) {
                    String string;
                    InputStreamReader reader2 = new InputStreamReader(inputStream);
                    try {
                        YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration((Reader)reader2);
                        string = pluginYml.getString("name");
                    }
                    catch (Throwable pluginYml) {
                        try {
                            try {
                                reader2.close();
                            }
                            catch (Throwable throwable) {
                                pluginYml.addSuppressed(throwable);
                            }
                            throw pluginYml;
                        }
                        catch (IOException reader2) {
                            break block18;
                        }
                        catch (IllegalArgumentException e) {
                            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error reading paper-plugin.yml: " + e.getMessage());
                        }
                    }
                    reader2.close();
                    return string;
                }
            }
            if ((inputStream = classLoader.getResourceAsStream("plugin.yml")) != null) {
                String string;
                InputStreamReader reader3 = new InputStreamReader(inputStream);
                try {
                    YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration((Reader)reader3);
                    string = pluginYml.getString("name");
                }
                catch (Throwable throwable) {
                    try {
                        try {
                            reader3.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                        throw throwable;
                    }
                    catch (IOException reader3) {
                        break block19;
                    }
                    catch (IllegalArgumentException e) {
                        MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error reading plugin.yml: " + e.getMessage());
                    }
                }
                reader3.close();
                return string;
            }
        }
        return NBTItem.class.getPackage().getName();
    }

    protected static String getPluginforBStats() {
        block19: {
            InputStream inputStream;
            ClassLoader classLoader;
            block18: {
                classLoader = VersionChecker.class.getClassLoader();
                inputStream = classLoader.getResourceAsStream("paper-plugin.yml");
                if (inputStream != null) {
                    String string;
                    InputStreamReader reader2 = new InputStreamReader(inputStream);
                    try {
                        YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration((Reader)reader2);
                        string = pluginYml.getString("name");
                    }
                    catch (Throwable pluginYml) {
                        try {
                            try {
                                reader2.close();
                            }
                            catch (Throwable throwable) {
                                pluginYml.addSuppressed(throwable);
                            }
                            throw pluginYml;
                        }
                        catch (IOException reader2) {
                            break block18;
                        }
                        catch (IllegalArgumentException e) {
                            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error reading paper-plugin.yml: " + e.getMessage());
                        }
                    }
                    reader2.close();
                    return string;
                }
            }
            if ((inputStream = classLoader.getResourceAsStream("plugin.yml")) != null) {
                String string;
                InputStreamReader reader3 = new InputStreamReader(inputStream);
                try {
                    YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration((Reader)reader3);
                    string = pluginYml.getString("name");
                }
                catch (Throwable throwable) {
                    try {
                        try {
                            reader3.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                        throw throwable;
                    }
                    catch (IOException reader3) {
                        break block19;
                    }
                    catch (IllegalArgumentException e) {
                        MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error reading plugin.yml: " + e.getMessage());
                    }
                }
                reader3.close();
                return string;
            }
        }
        return "UnknownPlugin";
    }

    protected static String getPluginType() {
        ClassLoader classLoader = VersionChecker.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("paper-plugin.yml");
        if (inputStream != null) {
            try {
                inputStream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return "PaperPlugin";
        }
        inputStream = classLoader.getResourceAsStream("plugin.yml");
        if (inputStream != null) {
            try {
                inputStream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return "SpigotPlugin";
        }
        return "UnknownPlugin";
    }
}

