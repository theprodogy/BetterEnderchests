/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 */
package com.fernsehheft.enderchest.nbtapi.utils.nmsmappings;

import com.fernsehheft.enderchest.nbtapi.NBTReflectionUtil;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import java.util.Optional;

public class CodecHelper {
    public static Object convertItemStackToNbt(Object itemStack) {
        DataResult result = null;
        try {
            result = NBTReflectionUtil.itemstack_codec.encodeStart(NBTReflectionUtil.nbtRegistryOps, itemStack);
            Objects.requireNonNull(result);
            return ((Optional)result.getClass().getMethod("result", new Class[0]).invoke((Object)result, new Object[0])).get();
        }
        catch (Exception e) {
            throw new NbtApiException("Failed to convert ItemStack to NBT. " + result + " " + itemStack, e);
        }
    }

    public static Object convertNbtToItemStack(Object nbt) {
        DataResult result = null;
        try {
            result = NBTReflectionUtil.itemstack_codec.parse(NBTReflectionUtil.nbtRegistryOps, nbt);
            Objects.requireNonNull(result);
            return ((Optional)result.getClass().getMethod("result", new Class[0]).invoke((Object)result, new Object[0])).get();
        }
        catch (Exception e) {
            throw new NbtApiException("Failed to convert NBT to ItemStack. " + result + " " + nbt, e);
        }
    }
}

