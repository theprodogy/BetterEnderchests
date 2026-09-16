/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.utils.metrics.charts;

import com.fernsehheft.enderchest.nbtapi.utils.metrics.charts.CustomChart;
import com.fernsehheft.enderchest.nbtapi.utils.metrics.json.JsonObjectBuilder;
import java.util.concurrent.Callable;

public class SingleLineChart
extends CustomChart {
    private final Callable<Integer> callable;

    public SingleLineChart(String chartId, Callable<Integer> callable) {
        super(chartId);
        this.callable = callable;
    }

    @Override
    protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
        int value = this.callable.call();
        if (value == 0) {
            return null;
        }
        return new JsonObjectBuilder().appendField("value", value).build();
    }
}

