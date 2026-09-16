/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.faststats.core;

import com.fernsehheft.enderchest.faststats.core.ErrorTracker;
import com.fernsehheft.enderchest.faststats.core.Token;
import com.fernsehheft.enderchest.faststats.core.data.Metric;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Async;
import org.jetbrains.annotations.Contract;

public interface Metrics {
    @Token
    @Contract(pure=true)
    public String getToken();

    @Contract(pure=true)
    public Optional<ErrorTracker> getErrorTracker();

    @Contract(pure=true)
    public Config getConfig();

    default public void ready() {
    }

    @Contract(mutates="this")
    public void shutdown();

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static interface Config {
        @Contract(pure=true)
        public UUID serverId();

        @Contract(pure=true)
        public boolean enabled();

        @Contract(pure=true)
        public boolean errorTracking();

        @Contract(pure=true)
        public boolean additionalMetrics();

        @Contract(pure=true)
        public boolean debug();
    }

    public static interface Factory<T, F extends Factory<T, F>> {
        @Contract(mutates="this")
        public F addMetric(Metric<?> var1) throws IllegalArgumentException;

        @Contract(mutates="this")
        public F onFlush(Runnable var1);

        @Contract(mutates="this")
        public F errorTracker(ErrorTracker var1);

        @Contract(mutates="this")
        public F debug(boolean var1);

        @Contract(mutates="this")
        public F token(@Token String var1) throws IllegalArgumentException;

        @Contract(mutates="this")
        public F url(URI var1);

        @Async.Schedule
        @Contract(value="_ -> new", mutates="io")
        public Metrics create(T var1) throws IllegalStateException;
    }
}

