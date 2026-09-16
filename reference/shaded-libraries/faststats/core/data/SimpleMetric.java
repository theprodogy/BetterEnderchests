/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.faststats.core.data;

import com.fernsehheft.enderchest.faststats.core.data.Metric;
import com.fernsehheft.enderchest.faststats.core.data.SourceId;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;

abstract class SimpleMetric<T>
implements Metric<T> {
    @SourceId
    private final String id;
    private final Callable<@Nullable T> callable;

    public SimpleMetric(@SourceId String id, Callable<@Nullable T> callable) throws IllegalArgumentException {
        if (!id.matches("[a-z_]+")) {
            throw new IllegalArgumentException("Invalid source id '" + id + "', must match '[a-z_]+'");
        }
        this.id = id;
        this.callable = callable;
    }

    @Override
    @SourceId
    public final String getId() {
        return this.id;
    }

    @Override
    public final Optional<T> compute() throws Exception {
        return Optional.ofNullable(this.callable.call());
    }

    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SimpleMetric that = (SimpleMetric)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    public String toString() {
        return "SimpleMetric{id='" + this.id + "'}";
    }
}

