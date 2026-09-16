/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.threadedregions.scheduler.ScheduledTask
 *  org.bukkit.plugin.Plugin
 */
package com.fernsehheft.enderchest.folialib.wrapper.task;

import com.fernsehheft.enderchest.folialib.wrapper.task.WrappedTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

public class WrappedFoliaTask
implements WrappedTask {
    private static final Class<? extends ScheduledTask> ASYNC_TASK_CLASS;
    private final ScheduledTask task;
    private final boolean async;

    public WrappedFoliaTask(ScheduledTask task) {
        this.task = task;
        this.async = ASYNC_TASK_CLASS == null ? false : ASYNC_TASK_CLASS.isAssignableFrom(task.getClass());
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
        return this.task.getOwningPlugin();
    }

    @Override
    public boolean isAsync() {
        return this.async;
    }

    static {
        Class<?> asyncTaskClass = null;
        try {
            asyncTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.FoliaAsyncScheduler.AsyncScheduledTask");
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        ASYNC_TASK_CLASS = asyncTaskClass;
    }
}

