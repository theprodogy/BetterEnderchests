/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonPrimitive
 */
package com.fernsehheft.enderchest.faststats.core.data;

import com.fernsehheft.enderchest.faststats.core.data.SimpleMetric;
import com.fernsehheft.enderchest.faststats.core.data.SourceId;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;

final class SingleValueMetric<T>
extends SimpleMetric<T> {
    public SingleValueMetric(@SourceId String id, Callable<@Nullable T> callable) throws IllegalArgumentException {
        super(id, callable);
    }

    @Override
    public Optional<JsonElement> getData() throws Exception {
        return this.compute().map(data -> {
            if (data instanceof Boolean) {
                Boolean bool = (Boolean)data;
                return new JsonPrimitive(bool);
            }
            if (data instanceof Number) {
                Number number = (Number)data;
                return new JsonPrimitive(number);
            }
            return new JsonPrimitive(data.toString());
        });
    }
}

