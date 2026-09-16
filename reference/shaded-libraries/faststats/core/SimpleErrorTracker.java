/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package com.fernsehheft.enderchest.faststats.core;

import com.fernsehheft.enderchest.faststats.core.ErrorHelper;
import com.fernsehheft.enderchest.faststats.core.ErrorTracker;
import com.fernsehheft.enderchest.faststats.core.MurmurHash3;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class SimpleErrorTracker
implements ErrorTracker {
    private final Map<String, Integer> collected = new ConcurrentHashMap<String, Integer>();
    private final Map<String, JsonObject> reports = new ConcurrentHashMap<String, JsonObject>();
    private final Map<Class<? extends Throwable>, Set<Pattern>> ignoredTypedPatterns = new ConcurrentHashMap<Class<? extends Throwable>, Set<Pattern>>();
    private final Set<Class<? extends Throwable>> ignoredTypes = new CopyOnWriteArraySet<Class<? extends Throwable>>();
    private final Set<Pattern> ignoredPatterns = new CopyOnWriteArraySet<Pattern>();
    private final List<Map.Entry<Pattern, String>> anonymizationEntries = new CopyOnWriteArrayList<Map.Entry<Pattern, String>>(List.of(Map.entry(ErrorHelper.ipv4Pattern(), "[IP hidden]"), Map.entry(ErrorHelper.ipv6Pattern(), "[IP hidden]"), Map.entry(ErrorHelper.userHomePathPattern(), "$1$2$3[username hidden]"), Map.entry(ErrorHelper.discordWebhookPattern(), "$1[token hidden]"), Map.entry(ErrorHelper.jdbcUrlPattern(), "$1[password hidden]$2")));
    private volatile @Nullable BiConsumer<@Nullable ClassLoader, Throwable> errorEvent = null;
    private volatile @Nullable Thread.UncaughtExceptionHandler originalHandler = null;

    public SimpleErrorTracker() {
        ErrorHelper.usernamePattern().ifPresent(pattern -> this.anonymizationEntries.add(Map.entry(pattern, "[username hidden]")));
    }

    @Override
    public void trackError(String message) {
        this.trackError(message, true);
    }

    @Override
    public void trackError(Throwable error) {
        this.trackError(error, true);
    }

    @Override
    public void trackError(String message, boolean handled) {
        this.trackError(new RuntimeException(message), handled);
    }

    @Override
    public void trackError(Throwable error, boolean handled) {
        try {
            if (this.isIgnored(error, Collections.newSetFromMap(new IdentityHashMap()))) {
                return;
            }
            JsonObject compiled = ErrorHelper.compile(error, null, handled, this.anonymizationEntries);
            String hashed = MurmurHash3.hash(compiled);
            if (this.collected.compute(hashed, (k, v) -> v == null ? 1 : v + 1) > 1) {
                return;
            }
            this.reports.put(hashed, compiled);
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
    }

    private boolean isIgnored(@Nullable Throwable error, Set<Throwable> visited) {
        String message;
        if (error == null || !visited.add(error)) {
            return false;
        }
        if (this.ignoredTypes.contains(error.getClass())) {
            return true;
        }
        String string = message = error.getMessage() != null ? error.getMessage() : "";
        if (this.ignoredPatterns.stream().map(pattern -> pattern.matcher(message)).anyMatch(Matcher::find)) {
            return true;
        }
        Set<Pattern> patterns = this.ignoredTypedPatterns.get(error.getClass());
        if (patterns != null && patterns.stream().map(pattern -> pattern.matcher(message)).anyMatch(Matcher::find)) {
            return true;
        }
        return this.isIgnored(error.getCause(), visited);
    }

    @Override
    public ErrorTracker ignoreError(Class<? extends Throwable> type) {
        this.ignoredTypes.add(type);
        return this;
    }

    @Override
    public ErrorTracker ignoreError(Pattern pattern) {
        this.ignoredPatterns.add(pattern);
        return this;
    }

    @Override
    public ErrorTracker ignoreError(Class<? extends Throwable> type, Pattern pattern) {
        this.ignoredTypedPatterns.computeIfAbsent(type, k -> new CopyOnWriteArraySet()).add(pattern);
        return this;
    }

    @Override
    public ErrorTracker anonymize(Pattern pattern, String replacement) {
        this.anonymizationEntries.add(Map.entry(pattern, replacement));
        return this;
    }

    public JsonArray getData(String buildId) {
        JsonArray report = new JsonArray(this.reports.size());
        this.reports.forEach((hash, object) -> {
            JsonObject copy = object.deepCopy();
            copy.addProperty("hash", hash);
            copy.addProperty("buildId", buildId);
            Integer count = this.collected.getOrDefault(hash, 1);
            if (count > 1) {
                copy.addProperty("count", (Number)count);
            }
            report.add((JsonElement)copy);
        });
        this.collected.forEach((hash, count) -> {
            if (count <= 0 || this.reports.containsKey(hash)) {
                return;
            }
            JsonObject entry = new JsonObject();
            entry.addProperty("hash", hash);
            if (count > 1) {
                entry.addProperty("count", (Number)count);
            }
            report.add((JsonElement)entry);
        });
        return report;
    }

    public void clear() {
        this.collected.replaceAll((k, v) -> 0);
        this.reports.clear();
    }

    @Override
    public synchronized void attachErrorContext(@Nullable ClassLoader loader) throws IllegalStateException {
        if (this.originalHandler != null) {
            throw new IllegalStateException("Error context already attached");
        }
        this.originalHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            Thread.UncaughtExceptionHandler handler = this.originalHandler;
            if (handler != null) {
                handler.uncaughtException(thread, error);
            }
            try {
                if (loader != null && !ErrorTracker.isSameLoader(loader, error)) {
                    return;
                }
                BiConsumer<ClassLoader, Throwable> event = this.errorEvent;
                if (event != null) {
                    event.accept(loader, error);
                }
                this.trackError(error, false);
            }
            catch (Throwable t) {
                this.trackError(t, false);
            }
        });
    }

    @Override
    public synchronized void detachErrorContext() {
        if (this.originalHandler == null) {
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler(this.originalHandler);
        this.originalHandler = null;
    }

    @Override
    public synchronized boolean isContextAttached() {
        return this.originalHandler != null;
    }

    @Override
    public synchronized void setContextErrorHandler(@Nullable BiConsumer<@Nullable ClassLoader, Throwable> errorEvent) {
        this.errorEvent = errorEvent;
    }

    @Override
    public synchronized Optional<BiConsumer<@Nullable ClassLoader, Throwable>> getContextErrorHandler() {
        return Optional.ofNullable(this.errorEvent);
    }
}

