/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.folialib.util;

import java.util.concurrent.TimeUnit;

public class TimeConverter {
    public static long toTicks(long time, TimeUnit unit) {
        return unit.toMillis(time) / 50L;
    }

    public static long toMillis(long ticks) {
        return ticks * 50L;
    }
}

