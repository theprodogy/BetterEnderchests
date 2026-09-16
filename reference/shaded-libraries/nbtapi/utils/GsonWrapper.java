/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package com.fernsehheft.enderchest.nbtapi.utils;

import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.google.gson.Gson;

@Deprecated
public class GsonWrapper {
    private static Gson gson = new Gson();

    private GsonWrapper() {
    }

    public static String getString(Object obj) {
        return gson.toJson(obj);
    }

    public static void overwriteGsonInstance(Gson replacement) {
        gson = replacement;
    }

    public static <T> T deserializeJson(String json, Class<T> type) {
        try {
            if (json == null) {
                return null;
            }
            Object obj = gson.fromJson(json, type);
            return type.cast(obj);
        }
        catch (Exception ex) {
            throw new NbtApiException("Error while converting json to " + type.getName(), ex);
        }
    }
}

