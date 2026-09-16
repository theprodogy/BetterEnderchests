/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.utils.metrics.charts;

import com.fernsehheft.enderchest.nbtapi.utils.metrics.json.JsonObjectBuilder;
import java.util.function.BiConsumer;

public abstract class CustomChart {
    private final String chartId;

    protected CustomChart(String chartId) {
        if (chartId == null) {
            throw new IllegalArgumentException("chartId must not be null");
        }
        this.chartId = chartId;
    }

    public JsonObjectBuilder.JsonObject getRequestJsonObject(BiConsumer<String, Throwable> errorLogger, boolean logErrors) {
        JsonObjectBuilder builder = new JsonObjectBuilder();
        builder.appendField("chartId", this.chartId);
        try {
            JsonObjectBuilder.JsonObject data = this.getChartData();
            if (data == null) {
                return null;
            }
            builder.appendField("data", data);
        }
        catch (Throwable t) {
            if (logErrors) {
                errorLogger.accept("Failed to get data for custom chart with id " + this.chartId, t);
            }
            return null;
        }
        return builder.build();
    }

    protected abstract JsonObjectBuilder.JsonObject getChartData() throws Exception;
}

