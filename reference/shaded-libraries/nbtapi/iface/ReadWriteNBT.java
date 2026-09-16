/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.bukkit.inventory.ItemStack
 */
package com.fernsehheft.enderchest.nbtapi.iface;

import com.fernsehheft.enderchest.nbtapi.iface.NBTHandler;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBTCompoundList;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBTList;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBT;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.inventory.ItemStack;

public interface ReadWriteNBT
extends ReadableNBT {
    public void mergeCompound(ReadableNBT var1);

    public void setString(String var1, String var2);

    public void setInteger(String var1, Integer var2);

    public void setDouble(String var1, Double var2);

    public void setByte(String var1, Byte var2);

    public void setShort(String var1, Short var2);

    public void setLong(String var1, Long var2);

    public void setFloat(String var1, Float var2);

    public void setByteArray(String var1, byte[] var2);

    public void setIntArray(String var1, int[] var2);

    public void setLongArray(String var1, long[] var2);

    public void setBoolean(String var1, Boolean var2);

    public void setItemStack(String var1, ItemStack var2);

    public void setItemStackArray(String var1, ItemStack[] var2);

    public void setUUID(String var1, UUID var2);

    public void removeKey(String var1);

    public ReadWriteNBT getOrCreateCompound(String var1);

    @Override
    @Nullable
    public ReadWriteNBT getCompound(String var1);

    public ReadWriteNBT resolveOrCreateCompound(String var1);

    public <T> void set(String var1, T var2, NBTHandler<T> var3);

    public <E extends Enum<?>> void setEnum(String var1, E var2);

    public ReadWriteNBTList<String> getStringList(String var1);

    public ReadWriteNBTList<Integer> getIntegerList(String var1);

    public ReadWriteNBTList<int[]> getIntArrayList(String var1);

    public ReadWriteNBTList<UUID> getUUIDList(String var1);

    public ReadWriteNBTList<Float> getFloatList(String var1);

    public ReadWriteNBTList<Double> getDoubleList(String var1);

    public ReadWriteNBTList<Long> getLongList(String var1);

    public ReadWriteNBTCompoundList getCompoundList(String var1);

    @Override
    @Nullable
    public ReadWriteNBT resolveCompound(String var1);

    public void clearNBT();
}

