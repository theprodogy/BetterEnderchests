/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 */
package com.fernsehheft.enderchest.nbtapi.iface;

import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBT;
import javax.annotation.Nonnull;

public interface NBTHandler<T> {
    default public boolean fuzzyMatch(Object obj) {
        return false;
    }

    public void set(@Nonnull ReadWriteNBT var1, @Nonnull String var2, @Nonnull T var3);

    public T get(@Nonnull ReadableNBT var1, @Nonnull String var2);
}

