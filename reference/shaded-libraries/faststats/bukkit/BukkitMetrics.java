/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.IllegalPluginAccessException
 *  org.bukkit.plugin.Plugin
 */
package com.fernsehheft.enderchest.faststats.bukkit;

import com.fernsehheft.enderchest.faststats.bukkit.BukkitMetricsImpl;
import com.fernsehheft.enderchest.faststats.core.Metrics;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;

/*
 * Uses 'sealed' constructs - enablewith --sealed true
 */
public interface BukkitMetrics
extends Metrics {
    @Contract(pure=true)
    public static Factory factory() {
        return new BukkitMetricsImpl.Factory();
    }

    @Override
    public void ready() throws IllegalPluginAccessException;

    public static interface Factory
    extends Metrics.Factory<Plugin, Factory> {
        public BukkitMetrics create(Plugin var1) throws IllegalStateException;
    }
}

