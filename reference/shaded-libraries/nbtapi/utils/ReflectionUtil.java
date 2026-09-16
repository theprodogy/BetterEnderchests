/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.utils;

import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.MojangToMapping;
import java.lang.reflect.Field;

public final class ReflectionUtil {
    public static Field getMappedField(Class<?> clazz, String mapping) {
        String mojmapName = mapping.split("#")[1];
        try {
            return clazz.getField(mojmapName);
        }
        catch (NoSuchFieldException | SecurityException exception) {
            try {
                return clazz.getDeclaredField(MojangToMapping.getMapping().get(mapping));
            }
            catch (Exception e) {
                throw new NbtApiException("Unable to find field " + mapping + " in class " + clazz.getName(), e);
            }
        }
    }
}

