/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.wrapper;

import com.fernsehheft.enderchest.nbtapi.iface.NBTHandler;
import com.fernsehheft.enderchest.nbtapi.wrapper.Casing;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface NBTProxy {
    public static final Map<Class<?>, NBTHandler<Object>> handlers = new HashMap();

    default public void init() {
    }

    default public Casing getCasing() {
        return Casing.PascalCase;
    }

    default public <T> NBTHandler<T> getHandler(Class<T> clazz) {
        return handlers.get(clazz);
    }

    default public Collection<NBTHandler<Object>> getHandlers() {
        return handlers.values();
    }

    default public <T> void registerHandler(Class<T> clazz, NBTHandler<T> handler) {
        handlers.put(clazz, handler);
    }
}

