/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.meta.ItemMeta
 */
package com.fernsehheft.enderchest.nbtapi.iface;

import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableItemNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBT;
import java.util.function.BiConsumer;
import org.bukkit.inventory.meta.ItemMeta;

public interface ReadWriteItemNBT
extends ReadWriteNBT,
ReadableItemNBT {
    public boolean hasCustomNbtData();

    public void clearCustomNBT();

    public void modifyMeta(BiConsumer<ReadableNBT, ItemMeta> var1);

    public <T extends ItemMeta> void modifyMeta(Class<T> var1, BiConsumer<ReadableNBT, T> var2);
}

