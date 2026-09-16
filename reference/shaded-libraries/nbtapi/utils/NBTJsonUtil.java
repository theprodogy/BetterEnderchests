/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.bukkit.inventory.ItemStack
 */
package com.fernsehheft.enderchest.nbtapi.utils;

import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ClassWrapper;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.MojangToMapping;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

public class NBTJsonUtil {
    public static JsonElement itemStackToJson(ItemStack itemStack) {
        try {
            Codec itemStackCodec = (Codec)ClassWrapper.NMS_ITEMSTACK.getClazz().getField(MojangToMapping.getMapping().get("net.minecraft.world.item.ItemStack#CODEC")).get(null);
            Object stack = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, itemStack);
            DataResult result = itemStackCodec.encode(stack, (DynamicOps)JsonOps.INSTANCE, (Object)((JsonElement)JsonOps.INSTANCE.emptyMap()));
            Optional opt = (Optional)result.getClass().getMethod("result", new Class[0]).invoke((Object)result, new Object[0]);
            return opt.orElse(null);
        }
        catch (Exception ex) {
            throw new NbtApiException("Error trying to get Json of an ItemStack.", ex);
        }
    }
}

