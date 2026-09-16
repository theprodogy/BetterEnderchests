/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBT;
import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTCompoundList;
import com.fernsehheft.enderchest.nbtapi.NBTContainer;
import com.fernsehheft.enderchest.nbtapi.NBTListCompound;
import com.fernsehheft.enderchest.nbtapi.NBTReflectionUtil;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteItemNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBT;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ClassWrapper;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NBTItem
extends NBTCompound
implements ReadWriteItemNBT {
    private ItemStack bukkitItem;
    private final boolean directApply;
    private final boolean finalizer;
    private ItemStack originalSrcStack = null;
    private Object cachedCompound = null;
    private boolean closed = false;

    @Deprecated
    public NBTItem(ItemStack item) {
        this(item, false);
    }

    protected NBTItem(ItemStack item, boolean directApply, boolean readOnly, boolean finalizer) {
        super(null, null, readOnly);
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            throw new NullPointerException("ItemStack can't be null/air/amount of 0! This is not a NBTAPI bug!");
        }
        this.finalizer = finalizer;
        if (finalizer) {
            this.bukkitItem = item;
            this.originalSrcStack = item;
            this.directApply = false;
        } else if (readOnly) {
            this.bukkitItem = item;
            this.directApply = false;
        } else {
            this.directApply = directApply;
            this.bukkitItem = item.clone();
            if (directApply) {
                this.originalSrcStack = item;
            }
        }
    }

    @Deprecated
    public NBTItem(ItemStack item, boolean directApply) {
        super(null, null);
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            throw new NullPointerException("ItemStack can't be null/air/amount of 0! This is not a NBTAPI bug!");
        }
        this.finalizer = false;
        this.directApply = directApply;
        this.bukkitItem = item.clone();
        if (directApply) {
            this.originalSrcStack = item;
        }
    }

    @Override
    public Object getCompound() {
        if (this.closed) {
            throw new NbtApiException("Tried using closed NBT data!");
        }
        if (this.isReadOnly() && (this.cachedCompound != null || ClassWrapper.CRAFT_ITEMSTACK.getClazz().isAssignableFrom(this.bukkitItem.getClass()))) {
            if (this.cachedCompound == null) {
                this.cachedCompound = NBTReflectionUtil.getItemRootNBTTagCompound(NBTReflectionUtil.getCraftItemHandle(this.bukkitItem));
            }
            return this.cachedCompound;
        }
        if (this.finalizer) {
            if (this.cachedCompound == null) {
                this.updateCachedCompound();
            }
            return this.cachedCompound;
        }
        return NBTReflectionUtil.getItemRootNBTTagCompound(ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, this.bukkitItem));
    }

    private void updateCachedCompound() {
        if (this.finalizer) {
            this.cachedCompound = NBTReflectionUtil.getItemRootNBTTagCompound(ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, this.bukkitItem));
        }
    }

    protected void finalizeChanges() {
        if (!this.finalizer || this.cachedCompound == null) {
            return;
        }
        if (NBTReflectionUtil.getKeys(this).isEmpty()) {
            this.cachedCompound = null;
        }
        if (ClassWrapper.CRAFT_ITEMSTACK.getClazz().isAssignableFrom(this.originalSrcStack.getClass())) {
            Object nmsStack = NBTReflectionUtil.getCraftItemHandle(this.originalSrcStack);
            NBTReflectionUtil.setItemStackCompound(nmsStack, this.cachedCompound);
            this.bukkitItem = this.originalSrcStack;
        } else {
            Object stack = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, this.bukkitItem);
            NBTReflectionUtil.setItemStackCompound(stack, this.cachedCompound);
            this.bukkitItem = (ItemStack)ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, stack);
            this.originalSrcStack.setItemMeta(this.bukkitItem.getItemMeta());
        }
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
    protected void setCompound(Object compound) {
        if (this.isReadOnly()) {
            throw new NbtApiException("Tried setting data in read only mode!");
        }
        if (this.closed) {
            throw new NbtApiException("Tried using closed NBT data!");
        }
        if (this.finalizer) {
            this.cachedCompound = compound;
            return;
        }
        if (compound != null && ((Set)ReflectionMethod.COMPOUND_GET_KEYS.run(compound, new Object[0])).isEmpty()) {
            compound = null;
        }
        if (ClassWrapper.CRAFT_ITEMSTACK.getClazz().isAssignableFrom(this.bukkitItem.getClass())) {
            Object nmsStack = NBTReflectionUtil.getCraftItemHandle(this.bukkitItem);
            NBTReflectionUtil.setItemStackCompound(nmsStack, compound);
        } else {
            Object stack = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, this.bukkitItem);
            NBTReflectionUtil.setItemStackCompound(stack, compound);
            this.bukkitItem = (ItemStack)ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, stack);
        }
    }

    @Deprecated
    public void applyNBT(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            throw new NullPointerException("ItemStack can't be null/Air! This is not a NBTAPI bug!");
        }
        NBTItem nbti = new NBTItem(new ItemStack(item.getType()));
        nbti.mergeCompound(this);
        item.setItemMeta(nbti.getItem().getItemMeta());
    }

    @Deprecated
    public void mergeNBT(ItemStack item) {
        NBTItem nbti = new NBTItem(item);
        nbti.mergeCompound(this);
        item.setItemMeta(nbti.getItem().getItemMeta());
    }

    @Deprecated
    public void mergeCustomNBT(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            throw new NullPointerException("ItemStack can't be null/Air!");
        }
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modify(item, nbt -> nbt.mergeCompound(this));
            return;
        }
        ItemMeta meta = item.getItemMeta();
        NBTReflectionUtil.getUnhandledNBTTags(meta).putAll(NBTReflectionUtil.getUnhandledNBTTags(this.bukkitItem.getItemMeta()));
        item.setItemMeta(meta);
    }

    @Override
    @Deprecated
    public boolean hasCustomNbtData() {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            return this.hasNBTData();
        }
        this.finalizeChanges();
        ItemMeta meta = this.bukkitItem.getItemMeta();
        return !NBTReflectionUtil.getUnhandledNBTTags(meta).isEmpty();
    }

    @Override
    @Deprecated
    public void clearCustomNBT() {
        this.finalizeChanges();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            this.setCompound(null);
            return;
        }
        ItemMeta meta = this.bukkitItem.getItemMeta();
        NBTReflectionUtil.getUnhandledNBTTags(meta).clear();
        this.bukkitItem.setItemMeta(meta);
        this.updateCachedCompound();
    }

    public ItemStack getItem() {
        return this.bukkitItem;
    }

    protected void setItem(ItemStack item) {
        this.bukkitItem = item;
    }

    @Override
    public boolean hasNBTData() {
        return this.getCompound() != null;
    }

    @Override
    public void modifyMeta(BiConsumer<ReadableNBT, ItemMeta> handler) {
        this.finalizeChanges();
        ItemMeta meta = this.bukkitItem.getItemMeta();
        handler.accept(new NBTContainer(this.getResolvedObject()).setReadOnly(true), meta);
        this.bukkitItem.setItemMeta(meta);
        this.updateCachedCompound();
        if (this.directApply) {
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                throw new NbtApiException("Direct apply mode meta changes don't work anymore in 1.20.5+. Please switch to the modern NBT.modify sytnax!");
            }
            this.applyNBT(this.originalSrcStack);
        }
    }

    @Override
    public <T extends ItemMeta> void modifyMeta(Class<T> type, BiConsumer<ReadableNBT, T> handler) {
        this.finalizeChanges();
        ItemMeta meta = this.bukkitItem.getItemMeta();
        handler.accept(new NBTContainer(this.getResolvedObject()).setReadOnly(true), (ReadableNBT)meta);
        this.bukkitItem.setItemMeta(meta);
        this.updateCachedCompound();
        if (this.directApply) {
            this.applyNBT(this.originalSrcStack);
        }
    }

    @Deprecated
    public static NBTContainer convertItemtoNBT(ItemStack item) {
        return NBTReflectionUtil.convertNMSItemtoNBTCompound(ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, item));
    }

    @Nullable
    @Deprecated
    public static ItemStack convertNBTtoItem(NBTCompound comp) {
        return (ItemStack)ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, NBTReflectionUtil.convertNBTCompoundtoNMSItem(comp));
    }

    @Deprecated
    public static NBTContainer convertItemArraytoNBT(ItemStack[] items) {
        NBTContainer container = new NBTContainer();
        container.setInteger("size", items.length);
        NBTCompoundList list = container.getCompoundList("items");
        for (int i = 0; i < items.length; ++i) {
            ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR) continue;
            NBTListCompound entry = list.addCompound();
            entry.setInteger("Slot", i);
            entry.mergeCompound(NBTItem.convertItemtoNBT(item));
        }
        return container;
    }

    @Nullable
    @Deprecated
    public static ItemStack[] convertNBTtoItemArray(NBTCompound comp) {
        if (!comp.hasTag("size")) {
            return null;
        }
        ItemStack[] rebuild = new ItemStack[comp.getInteger("size").intValue()];
        for (int i = 0; i < rebuild.length; ++i) {
            rebuild[i] = new ItemStack(Material.AIR);
        }
        if (!comp.hasTag("items")) {
            return rebuild;
        }
        NBTCompoundList list = comp.getCompoundList("items");
        for (ReadWriteNBT lcomp : list) {
            if (!(lcomp instanceof NBTCompound)) continue;
            int slot = lcomp.getInteger("Slot");
            rebuild[slot] = NBTItem.convertNBTtoItem((NBTCompound)lcomp);
        }
        return rebuild;
    }

    @Override
    protected void saveCompound() {
        if (this.directApply) {
            this.applyNBT(this.originalSrcStack);
        }
    }
}

