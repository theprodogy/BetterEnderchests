/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 */
package com.fernsehheft.enderchest.faststats.core.data;

import com.fernsehheft.enderchest.faststats.core.data.SimpleMetric;
import com.fernsehheft.enderchest.faststats.core.data.SourceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;

final class ArrayMetric<T>
extends SimpleMetric<T[]> {
    public ArrayMetric(@SourceId String id, Callable<T @Nullable []> callable) throws IllegalArgumentException {
        super(id, callable);
    }

    @Override
    public Optional<JsonElement> getData() throws Exception {
        return this.compute().map(data -> {
            JsonArray elements = new JsonArray(((Object[])data).length);
            for (Object d : data) {
                if (d instanceof Boolean) {
                    Boolean b = (Boolean)d;
                    elements.add(b);
                    continue;
                }
                if (d instanceof Number) {
                    Number n = (Number)d;
                    elements.add(n);
                    continue;
                }
                elements.add(d.toString());
            }
            return elements;
        });
    }
}

