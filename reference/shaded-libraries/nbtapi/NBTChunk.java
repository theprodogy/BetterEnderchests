/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Chunk
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTPersistentDataContainer;
import com.fernsehheft.enderchest.nbtapi.utils.CheckUtil;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import org.bukkit.Chunk;

public class NBTChunk {
    private final Chunk chunk;

    public NBTChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public NBTCompound getPersistentDataContainer() {
        CheckUtil.assertAvailable(MinecraftVersion.MC1_16_R3);
        return new NBTPersistentDataContainer(this.chunk.getPersistentDataContainer());
    }
}

