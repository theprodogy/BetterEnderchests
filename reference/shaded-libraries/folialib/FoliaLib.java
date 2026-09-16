/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.fernsehheft.enderchest.folialib;

import com.fernsehheft.enderchest.folialib.enums.ImplementationType;
import com.fernsehheft.enderchest.folialib.impl.PlatformScheduler;
import com.fernsehheft.enderchest.folialib.util.InvalidTickDelayNotifier;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class FoliaLib {
    private final JavaPlugin plugin;
    private final ImplementationType implementationType;
    private final PlatformScheduler scheduler;

    public FoliaLib(JavaPlugin plugin) {
        this.plugin = plugin;
        ImplementationType foundType = ImplementationType.UNKNOWN;
        for (ImplementationType type : ImplementationType.values()) {
            if (!type.selfCheck()) continue;
            foundType = type;
            break;
        }
        this.implementationType = foundType;
        this.scheduler = this.createServerImpl(this.implementationType.getImplementationClassName());
        if (this.scheduler == null) {
            throw new IllegalStateException("Failed to create server implementation. Please report this to the FoliaLib GitHub issues page. Forks of server software may not all be supported. If you are using an unofficial fork, please report this to the fork's developers first.");
        }
        String originalLocation = "com,tcoded,folialib,".replace(",", ".");
        if (this.getClass().getName().startsWith(originalLocation)) {
            Logger logger = this.plugin.getLogger();
            logger.severe("****************************************************************");
            logger.severe("FoliaLib is not be relocated correctly! This will cause conflicts");
            logger.severe("with other plugins using FoliaLib. Please contact the developers");
            logger.severe(String.format("of '%s' and inform them of this issue immediately!", this.plugin.getDescription().getName()));
            logger.severe("****************************************************************");
        }
    }

    public ImplementationType getImplType() {
        return this.implementationType;
    }

    @Deprecated
    public PlatformScheduler getImpl() {
        return this.getScheduler();
    }

    public PlatformScheduler getScheduler() {
        return this.scheduler;
    }

    public boolean isFolia() {
        return this.implementationType == ImplementationType.FOLIA;
    }

    public boolean isPaper() {
        return this.implementationType == ImplementationType.PAPER || this.implementationType == ImplementationType.LEGACY_PAPER;
    }

    public boolean isSpigot() {
        return this.implementationType == ImplementationType.SPIGOT || this.implementationType == ImplementationType.LEGACY_SPIGOT;
    }

    public boolean isUnsupported() {
        return this.implementationType == ImplementationType.UNKNOWN;
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    public void disableInvalidTickValueWarning() {
        InvalidTickDelayNotifier.disableNotifications = true;
    }

    public void enableInvalidTickValueDebug() {
        InvalidTickDelayNotifier.debugMode = true;
    }

    private PlatformScheduler createServerImpl(String implName) {
        String basePackage = this.getClass().getPackage().getName() + ".impl.";
        try {
            return (PlatformScheduler)Class.forName(basePackage + implName).getConstructor(this.getClass()).newInstance(this);
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}

