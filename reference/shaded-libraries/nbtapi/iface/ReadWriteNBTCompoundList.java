/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.iface;

import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBTList;
import java.util.function.Predicate;

public interface ReadWriteNBTCompoundList
extends ReadableNBTList<ReadWriteNBT> {
    public ReadWriteNBT addCompound();

    public ReadWriteNBT addCompound(ReadableNBT var1);

    public ReadWriteNBT remove(int var1);

    public void clear();

    public boolean removeIf(Predicate<? super ReadWriteNBT> var1);
}

