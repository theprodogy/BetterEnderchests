/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fernsehheft.enderchest.folialib.wrapper.task;

import com.fernsehheft.enderchest.folialib.wrapper.task.WrappedTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class WrappedBukkitTask
implements WrappedTask {
    private final BukkitTask task;

    public WrappedBukkitTask(BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        this.task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return this.task.isCancelled();
    }

    @Override
    public Plugin getOwningPlugin() {
        return this.task.getOwner();
    }

    @Override
    public boolean isAsync() {
        return !this.task.isSync();
    }
}

