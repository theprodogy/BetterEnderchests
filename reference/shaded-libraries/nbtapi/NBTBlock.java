/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.block.Block
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTChunk;
import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import org.bukkit.block.Block;

public class NBTBlock {
    private final Block block;
    private final NBTChunk nbtChunk;

    public NBTBlock(Block block) {
        this.block = block;
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3)) {
            throw new NbtApiException("NBTBlock is only working for 1.16.4+!");
        }
        this.nbtChunk = new NBTChunk(block.getChunk());
    }

    public NBTCompound getData() {
        return this.nbtChunk.getPersistentDataContainer().getOrCreateCompound("blocks").getOrCreateCompound(this.block.getX() + "_" + this.block.getY() + "_" + this.block.getZ());
    }
}

