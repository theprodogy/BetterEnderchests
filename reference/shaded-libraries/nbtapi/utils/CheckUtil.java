/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.utils;

import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;

public class CheckUtil {
    private CheckUtil() {
    }

    public static void assertAvailable(MinecraftVersion version) {
        if (!MinecraftVersion.isAtLeastVersion(version)) {
            throw new NbtApiException("This Method is only avaliable for the version " + version.name() + " and above!");
        }
    }
}

