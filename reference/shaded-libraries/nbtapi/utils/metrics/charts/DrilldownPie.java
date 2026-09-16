/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.utils.metrics.charts;

import com.fernsehheft.enderchest.nbtapi.utils.metrics.charts.CustomChart;
import com.fernsehheft.enderchest.nbtapi.utils.metrics.json.JsonObjectBuilder;
import java.util.Map;
import java.util.concurrent.Callable;

public class DrilldownPie
extends CustomChart {
    private final Callable<Map<String, Map<String, Integer>>> callable;

    public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
        super(chartId);
        this.callable = callable;
    }

    @Override
    public JsonObjectBuilder.JsonObject getChartData() throws Exception {
        JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
        Map<String, Map<String, Integer>> map = this.callable.call();
        if (map == null || map.isEmpty()) {
            return null;
        }
        boolean reallyAllSkipped = true;
        for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
            JsonObjectBuilder valueBuilder = new JsonObjectBuilder();
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                valueBuilder.appendField(valueEntry.getKey(), valueEntry.getValue());
                allSkipped = false;
            }
            if (allSkipped) continue;
            reallyAllSkipped = false;
            valuesBuilder.appendField(entryValues.getKey(), valueBuilder.build());
        }
        if (reallyAllSkipped) {
            return null;
        }
        return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
    }
}

