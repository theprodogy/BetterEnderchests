/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.block.BlockState
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTContainer;
import com.fernsehheft.enderchest.nbtapi.NBTReflectionUtil;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.utils.CheckUtil;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;

public class NBTTileEntity
extends NBTCompound {
    private final BlockState tile;
    private final boolean readonly;
    private final Object compound;
    private boolean closed = false;

    protected NBTTileEntity(BlockState tile, boolean readonly) {
        super(null, null);
        if (tile == null || MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3) && !tile.isPlaced()) {
            throw new NullPointerException("Tile can't be null/not placed!");
        }
        this.tile = tile;
        this.readonly = readonly;
        this.compound = readonly ? this.getCompound() : null;
    }

    @Deprecated
    public NBTTileEntity(BlockState tile) {
        super(null, null);
        if (tile == null || MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3) && !tile.isPlaced()) {
            throw new NullPointerException("Tile can't be null/not placed!");
        }
        this.readonly = false;
        this.compound = null;
        this.tile = tile;
    }

    @Override
    protected void setClosed() {
        this.closed = true;
    }

    @Override
    protected boolean isClosed() {
        return this.closed;
    }

    @Override
    protected boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public Object getCompound() {
        if (this.readonly && this.compound != null) {
            return this.compound;
        }
        if (!Bukkit.isPrimaryThread()) {
            throw new NbtApiException("BlockEntity NBT needs to be accessed sync!");
        }
        return NBTReflectionUtil.getTileEntityNBTTagCompound(this.tile);
    }

    @Override
    protected void setCompound(Object compound) {
        if (this.readonly) {
            throw new NbtApiException("Tried setting data in read only mode!");
        }
        if (!Bukkit.isPrimaryThread()) {
            throw new NbtApiException("BlockEntity NBT needs to be accessed sync!");
        }
        NBTReflectionUtil.setTileEntityNBTTagCompound(this.tile, compound);
    }

    public NBTCompound getPersistentDataContainer() {
        CheckUtil.assertAvailable(MinecraftVersion.MC1_14_R1);
        if (this.hasTag("PublicBukkitValues")) {
            return this.getCompound("PublicBukkitValues");
        }
        NBTContainer container = new NBTContainer();
        container.addCompound("PublicBukkitValues").setString("__nbtapi", "Marker to make the PersistentDataContainer have content");
        this.mergeCompound(container);
        return this.getCompound("PublicBukkitValues");
    }
}

