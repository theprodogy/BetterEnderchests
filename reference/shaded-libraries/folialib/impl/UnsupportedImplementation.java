/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.fernsehheft.enderchest.folialib.impl;

import com.fernsehheft.enderchest.folialib.FoliaLib;
import com.fernsehheft.enderchest.folialib.impl.LegacySpigotImplementation;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class UnsupportedImplementation
extends LegacySpigotImplementation {
    public UnsupportedImplementation(FoliaLib foliaLib) {
        super(foliaLib);
        JavaPlugin plugin = foliaLib.getPlugin();
        Logger logger = plugin.getLogger();
        logger.warning(String.format("\n---------------------------------------------------------------------\nFoliaLib does not support this server software! (%s)\nFoliaLib will attempt to use the legacy spigot implementation.\n---------------------------------------------------------------------\n", plugin.getServer().getVersion()));
    }
}

