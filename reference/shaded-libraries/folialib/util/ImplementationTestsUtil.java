/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Entity
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitScheduler
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fernsehheft.enderchest.folialib.util;

import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class ImplementationTestsUtil {
    private static final boolean IS_CANCELLED_SUPPORTED;
    private static final boolean IS_TASK_CONSUMERS_SUPPORTED;
    private static final boolean IS_ASYNC_TELEPORT_SUPPORTED;

    public static boolean isCancelledSupported() {
        return IS_CANCELLED_SUPPORTED;
    }

    public static boolean isTaskConsumersSupported() {
        return IS_TASK_CONSUMERS_SUPPORTED;
    }

    public static boolean isAsyncTeleportSupported() {
        return IS_ASYNC_TELEPORT_SUPPORTED;
    }

    static {
        boolean isCancelledSupported = false;
        try {
            Class<BukkitTask> bukkitTaskClass = BukkitTask.class;
            bukkitTaskClass.getDeclaredMethod("isCancelled", new Class[0]);
            isCancelledSupported = true;
        }
        catch (NoSuchMethodException bukkitTaskClass) {
            // empty catch block
        }
        IS_CANCELLED_SUPPORTED = isCancelledSupported;
        boolean taskConsumersSupported = false;
        try {
            Class<BukkitScheduler> bukkitSchedulerClass = BukkitScheduler.class;
            bukkitSchedulerClass.getDeclaredMethod("runTask", Plugin.class, Consumer.class);
            taskConsumersSupported = true;
        }
        catch (NoSuchMethodException bukkitSchedulerClass) {
            // empty catch block
        }
        IS_TASK_CONSUMERS_SUPPORTED = taskConsumersSupported;
        boolean isAsyncTeleportSupported = false;
        try {
            Class<Entity> entityClass = Entity.class;
            entityClass.getDeclaredMethod("teleportAsync", Location.class, PlayerTeleportEvent.TeleportCause.class);
            isAsyncTeleportSupported = true;
        }
        catch (NoSuchMethodException noSuchMethodException) {
            // empty catch block
        }
        IS_ASYNC_TELEPORT_SUPPORTED = isAsyncTeleportSupported;
    }
}

