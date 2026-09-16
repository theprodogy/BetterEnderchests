/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.destroystokyo.paper.event.server.ServerExceptionEvent
 *  com.destroystokyo.paper.exception.ServerException
 *  com.destroystokyo.paper.exception.ServerPluginException
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 */
package com.fernsehheft.enderchest.faststats.bukkit;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerException;
import com.destroystokyo.paper.exception.ServerPluginException;
import com.fernsehheft.enderchest.faststats.bukkit.BukkitMetricsImpl;
import com.fernsehheft.enderchest.faststats.core.ErrorTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

record PaperEventListener(BukkitMetricsImpl metrics) implements Listener
{
    @EventHandler(priority=EventPriority.MONITOR)
    public void onServerException(ServerExceptionEvent event) {
        ServerException serverException = event.getException();
        if (!(serverException instanceof ServerPluginException)) {
            return;
        }
        ServerPluginException exception = (ServerPluginException)serverException;
        if (!exception.getResponsiblePlugin().equals((Object)this.metrics.plugin())) {
            return;
        }
        Object report = exception.getCause() != null ? exception.getCause() : exception;
        this.metrics.getErrorTracker().ifPresent(arg_0 -> PaperEventListener.lambda$onServerException$0((Throwable)report, arg_0));
    }

    private static /* synthetic */ void lambda$onServerException$0(Throwable report, ErrorTracker tracker) {
        tracker.trackError(report, false);
    }
}

