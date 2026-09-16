/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.bukkit.inventory.ItemStack
 */
package com.fernsehheft.enderchest.nbtapi.iface;

import com.fernsehheft.enderchest.nbtapi.NBTType;
import com.fernsehheft.enderchest.nbtapi.iface.NBTHandler;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBTList;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.inventory.ItemStack;

public interface ReadableNBT {
    public String getString(String var1);

    public Integer getInteger(String var1);

    public Double getDouble(String var1);

    public Byte getByte(String var1);

    public Short getShort(String var1);

    public Long getLong(String var1);

    public Float getFloat(String var1);

    @Nullable
    public byte[] getByteArray(String var1);

    @Nullable
    public int[] getIntArray(String var1);

    @Nullable
    public long[] getLongArray(String var1);

    public Boolean getBoolean(String var1);

    @Nullable
    public ItemStack getItemStack(String var1);

    @Nullable
    public ItemStack[] getItemStackArray(String var1);

    @Nullable
    public UUID getUUID(String var1);

    public boolean hasTag(String var1);

    default public boolean hasTag(String key, NBTType type) {
        return this.hasTag(key) && this.getType(key) == type;
    }

    public Set<String> getKeys();

    @Nullable
    public ReadableNBT getCompound(String var1);

    public ReadableNBTList<String> getStringList(String var1);

    public ReadableNBTList<Integer> getIntegerList(String var1);

    public ReadableNBTList<int[]> getIntArrayList(String var1);

    public ReadableNBTList<UUID> getUUIDList(String var1);

    public ReadableNBTList<Float> getFloatList(String var1);

    public ReadableNBTList<Double> getDoubleList(String var1);

    public ReadableNBTList<Long> getLongList(String var1);

    @Nullable
    public NBTType getListType(String var1);

    public ReadableNBTList<ReadWriteNBT> getCompoundList(String var1);

    public <T> T getOrDefault(String var1, T var2);

    @Nullable
    public <T> T getOrNull(String var1, Class<?> var2);

    @Nullable
    public <T> T resolveOrNull(String var1, Class<?> var2);

    public <T> T resolveOrDefault(String var1, T var2);

    @Nullable
    public ReadableNBT resolveCompound(String var1);

    public <T> T get(String var1, NBTHandler<T> var2);

    @Nullable
    public <E extends Enum<E>> E getEnum(String var1, Class<E> var2);

    public NBTType getType(String var1);

    public void writeCompound(OutputStream var1);

    public ReadWriteNBT extractDifference(ReadableNBT var1);

    public String toString();
}

