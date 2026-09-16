/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 */
package com.fernsehheft.enderchest.faststats.core.data;

import com.fernsehheft.enderchest.faststats.core.data.ArrayMetric;
import com.fernsehheft.enderchest.faststats.core.data.SingleValueMetric;
import com.fernsehheft.enderchest.faststats.core.data.SourceId;
import com.google.gson.JsonElement;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface Metric<T> {
    @SourceId
    @Contract(pure=true)
    public String getId();

    @Contract(pure=true)
    public Optional<T> compute() throws Exception;

    @Contract(pure=true)
    public Optional<JsonElement> getData() throws Exception;

    @Contract(value="_, _ -> new", pure=true)
    public static Metric<String[]> stringArray(@SourceId String id, Callable<String @Nullable []> callable) throws IllegalArgumentException {
        return new ArrayMetric(id, (Callable<T[]>)callable);
    }

    @Contract(value="_, _ -> new", pure=true)
    public static Metric<Boolean[]> booleanArray(@SourceId String id, Callable<Boolean @Nullable []> callable) throws IllegalArgumentException {
        return new ArrayMetric(id, (Callable<T[]>)callable);
    }

    @Contract(value="_, _ -> new", pure=true)
    public static Metric<Number[]> numberArray(@SourceId String id, Callable<Number @Nullable []> callable) throws IllegalArgumentException {
        return new ArrayMetric(id, (Callable<T[]>)callable);
    }

    @Contract(value="_, _ -> new", pure=true)
    public static Metric<Boolean> bool(@SourceId String id, Callable<@Nullable Boolean> callable) throws IllegalArgumentException {
        return new SingleValueMetric<Boolean>(id, callable);
    }

    @Contract(value="_, _ -> new", pure=true)
    public static Metric<String> string(@SourceId String id, Callable<@Nullable String> callable) throws IllegalArgumentException {
        return new SingleValueMetric<String>(id, callable);
    }

    @Contract(value="_, _ -> new", pure=true)
    public static Metric<Number> number(@SourceId String id, Callable<@Nullable Number> callable) throws IllegalArgumentException {
        return new SingleValueMetric<Number>(id, callable);
    }
}

