/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.faststats.core;

import com.fernsehheft.enderchest.faststats.core.ErrorHelper;
import com.fernsehheft.enderchest.faststats.core.SimpleErrorTracker;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/*
 * Uses 'sealed' constructs - enablewith --sealed true
 */
public interface ErrorTracker {
    @Contract(value=" -> new")
    public static ErrorTracker contextAware() {
        SimpleErrorTracker tracker = new SimpleErrorTracker();
        tracker.attachErrorContext(ErrorTracker.class.getClassLoader());
        return tracker;
    }

    @Contract(value=" -> new")
    public static ErrorTracker contextUnaware() {
        return new SimpleErrorTracker();
    }

    @Contract(mutates="this")
    public void trackError(String var1);

    @Contract(mutates="this")
    public void trackError(Throwable var1);

    @Contract(mutates="this")
    public void trackError(String var1, boolean var2);

    @Contract(mutates="this")
    public void trackError(Throwable var1, boolean var2);

    @Contract(value="_ -> this", mutates="this")
    public ErrorTracker ignoreError(Class<? extends Throwable> var1);

    @Contract(value="_ -> this", mutates="this")
    public ErrorTracker ignoreError(Pattern var1);

    @Contract(value="_ -> this", mutates="this")
    default public ErrorTracker ignoreError(@RegExp String pattern) {
        return this.ignoreError(Pattern.compile(pattern));
    }

    @Contract(value="_, _ -> this", mutates="this")
    public ErrorTracker ignoreError(Class<? extends Throwable> var1, Pattern var2);

    @Contract(value="_, _ -> this", mutates="this")
    default public ErrorTracker ignoreError(Class<? extends Throwable> type, @RegExp String pattern) {
        return this.ignoreError(type, Pattern.compile(pattern));
    }

    @Contract(value="_, _ -> this", mutates="this")
    public ErrorTracker anonymize(Pattern var1, String var2);

    @Contract(value="_, _ -> this", mutates="this")
    default public ErrorTracker anonymize(@RegExp String pattern, String replacement) {
        return this.anonymize(Pattern.compile(pattern), replacement);
    }

    public void attachErrorContext(@Nullable ClassLoader var1) throws IllegalStateException;

    public void detachErrorContext();

    public boolean isContextAttached();

    @Contract(mutates="this")
    public void setContextErrorHandler(@Nullable BiConsumer<@Nullable ClassLoader, Throwable> var1);

    @Contract(pure=true)
    public Optional<BiConsumer<@Nullable ClassLoader, Throwable>> getContextErrorHandler();

    @Contract(pure=true)
    public static boolean isSameLoader(ClassLoader loader, Throwable error) {
        return ErrorHelper.isSameLoader(loader, error);
    }
}

