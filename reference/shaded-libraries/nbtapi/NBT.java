/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  javax.annotation.Nullable
 *  org.bukkit.block.BlockState
 *  org.bukkit.entity.Entity
 *  org.bukkit.inventory.ItemStack
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTContainer;
import com.fernsehheft.enderchest.nbtapi.NBTEntity;
import com.fernsehheft.enderchest.nbtapi.NBTFile;
import com.fernsehheft.enderchest.nbtapi.NBTGameProfile;
import com.fernsehheft.enderchest.nbtapi.NBTItem;
import com.fernsehheft.enderchest.nbtapi.NBTTileEntity;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.iface.NBTFileHandle;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteItemNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableItemNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBTList;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ClassWrapper;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import com.fernsehheft.enderchest.nbtapi.wrapper.NBTProxy;
import com.fernsehheft.enderchest.nbtapi.wrapper.ProxyBuilder;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class NBT {
    private NBT() {
    }

    public static boolean preloadApi() {
        try {
            if (MinecraftVersion.getVersion() == MinecraftVersion.UNKNOWN) {
                NbtApiException.confirmedBroken = true;
                return false;
            }
            for (ClassWrapper classWrapper : ClassWrapper.values()) {
                if (!classWrapper.isEnabled() || classWrapper.getClazz() != null) continue;
                NbtApiException.confirmedBroken = true;
                return false;
            }
            for (Enum enum_ : ReflectionMethod.values()) {
                if (!((ReflectionMethod)enum_).isCompatible() || ((ReflectionMethod)enum_).isLoaded()) continue;
                NbtApiException.confirmedBroken = true;
                return false;
            }
            return true;
        }
        catch (Exception ex) {
            NbtApiException.confirmedBroken = true;
            MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error during the selfcheck!", ex);
            return false;
        }
    }

    public static ReadableNBT readNbt(ItemStack item) {
        return new NBTItem(item.clone(), false, true, false);
    }

    public static <T> T get(ItemStack item, Function<ReadableItemNBT, T> getter) {
        NBTItem nbt = new NBTItem(item, false, true, false);
        T ret = getter.apply(nbt);
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        nbt.setClosed();
        return ret;
    }

    public static void get(ItemStack item, Consumer<ReadableItemNBT> getter) {
        NBTItem nbt = new NBTItem(item, false, true, false);
        getter.accept(nbt);
        nbt.setClosed();
    }

    public static <T> T get(Entity entity, Function<ReadableNBT, T> getter) {
        NBTEntity nbt = new NBTEntity(entity, true);
        T ret = getter.apply(nbt);
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        nbt.setClosed();
        return ret;
    }

    public static void get(Entity entity, Consumer<ReadableNBT> getter) {
        NBTEntity nbt = new NBTEntity(entity, true);
        getter.accept(nbt);
        nbt.setClosed();
    }

    public static <T> T get(BlockState blockState, Function<ReadableNBT, T> getter) {
        NBTTileEntity nbt = new NBTTileEntity(blockState, true);
        T ret = getter.apply(nbt);
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        nbt.setClosed();
        return ret;
    }

    public static void get(BlockState blockState, Consumer<ReadableNBT> getter) {
        NBTTileEntity nbt = new NBTTileEntity(blockState, true);
        getter.accept(nbt);
        nbt.setClosed();
    }

    public static <T> T getPersistentData(Entity entity, Function<ReadableNBT, T> getter) {
        T ret = getter.apply(new NBTEntity(entity).getPersistentDataContainer());
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        return ret;
    }

    public static <T> T getPersistentData(BlockState blockState, Function<ReadableNBT, T> getter) {
        T ret = getter.apply(new NBTTileEntity(blockState).getPersistentDataContainer());
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        return ret;
    }

    public static <T> T modify(ItemStack item, Function<ReadWriteItemNBT, T> function) {
        NBTItem nbti = new NBTItem(item, false, false, true);
        T val = function.apply(nbti);
        nbti.finalizeChanges();
        if (val instanceof ReadableNBT || val instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        nbti.setClosed();
        return val;
    }

    public static void modify(ItemStack item, Consumer<ReadWriteItemNBT> consumer) {
        NBTItem nbti = new NBTItem(item, false, false, true);
        consumer.accept(nbti);
        nbti.finalizeChanges();
        nbti.setClosed();
    }

    public static <T> T modify(Entity entity, Function<ReadWriteNBT, T> function) {
        NBTEntity nbtEnt = new NBTEntity(entity);
        NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
        T ret = function.apply(cont);
        nbtEnt.setCompound(cont.getCompound());
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        nbtEnt.setClosed();
        return ret;
    }

    public static void modifyComponents(ItemStack item, Consumer<ReadWriteNBT> consumer) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            throw new NbtApiException("This method only works for 1.20.5+!");
        }
        ReadWriteNBT nbti = NBT.itemStackToNBT(item);
        consumer.accept(nbti.getOrCreateCompound("components"));
        ItemStack tmp = NBT.itemStackFromNBT(nbti);
        item.setItemMeta(tmp.getItemMeta());
    }

    public static <T> T modifyComponents(ItemStack item, Function<ReadWriteNBT, T> function) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            throw new NbtApiException("This method only works for 1.20.5+!");
        }
        ReadWriteNBT nbti = NBT.itemStackToNBT(item);
        T ret = function.apply(nbti.getOrCreateCompound("components"));
        ItemStack tmp = NBT.itemStackFromNBT(nbti);
        item.setItemMeta(tmp.getItemMeta());
        return ret;
    }

    public static void getComponents(ItemStack item, Consumer<ReadableNBT> consumer) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            throw new NbtApiException("This method only works for 1.20.5+!");
        }
        ReadWriteNBT nbti = NBT.itemStackToNBT(item);
        consumer.accept(nbti.getOrCreateCompound("components"));
    }

    public static <T> T getComponents(ItemStack item, Function<ReadableNBT, T> function) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            throw new NbtApiException("This method only works for 1.20.5+!");
        }
        ReadWriteNBT nbti = NBT.itemStackToNBT(item);
        return function.apply(nbti.getOrCreateCompound("components"));
    }

    public static void modify(Entity entity, Consumer<ReadWriteNBT> consumer) {
        NBTEntity nbtEnt = new NBTEntity(entity);
        NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
        consumer.accept(cont);
        nbtEnt.setCompound(cont.getCompound());
        nbtEnt.setClosed();
    }

    public static <T> T modifyPersistentData(Entity entity, Function<ReadWriteNBT, T> function) {
        T ret = function.apply(new NBTEntity(entity).getPersistentDataContainer());
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        return ret;
    }

    public static void modifyPersistentData(Entity entity, Consumer<ReadWriteNBT> consumer) {
        consumer.accept(new NBTEntity(entity).getPersistentDataContainer());
    }

    public static <T> T modify(BlockState blockState, Function<ReadWriteNBT, T> function) {
        NBTTileEntity blockEnt = new NBTTileEntity(blockState);
        NBTContainer cont = new NBTContainer(blockEnt.getCompound());
        T ret = function.apply(cont);
        blockEnt.setCompound(cont.getCompound());
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        blockEnt.setClosed();
        return ret;
    }

    public static void modify(BlockState blockState, Consumer<ReadWriteNBT> consumer) {
        NBTTileEntity blockEnt = new NBTTileEntity(blockState);
        NBTContainer cont = new NBTContainer(blockEnt.getCompound());
        consumer.accept(cont);
        blockEnt.setCompound(cont.getCompound());
        blockEnt.setClosed();
    }

    public static <T> T modifyPersistentData(BlockState blockState, Function<ReadWriteNBT, T> function) {
        T ret = function.apply(new NBTTileEntity(blockState).getPersistentDataContainer());
        if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        return ret;
    }

    public static void modifyPersistentData(BlockState blockState, Consumer<ReadWriteNBT> consumer) {
        consumer.accept(new NBTTileEntity(blockState).getPersistentDataContainer());
    }

    public static ReadWriteNBT gameProfileToNBT(GameProfile profile) {
        return NBTGameProfile.toNBT(profile);
    }

    public static GameProfile gameProfileFromNBT(ReadableNBT compound) {
        return NBTGameProfile.fromNBT((NBTCompound)compound);
    }

    public static ReadWriteNBT itemStackToNBT(ItemStack itemStack) {
        return NBTItem.convertItemtoNBT(itemStack);
    }

    @Nullable
    public static ItemStack itemStackFromNBT(ReadableNBT compound) {
        return NBTItem.convertNBTtoItem((NBTCompound)compound);
    }

    public static ReadWriteNBT itemStackArrayToNBT(ItemStack[] itemStacks) {
        return NBTItem.convertItemArraytoNBT(itemStacks);
    }

    @Nullable
    public static ItemStack[] itemStackArrayFromNBT(ReadableNBT compound) {
        return NBTItem.convertNBTtoItemArray((NBTCompound)compound);
    }

    public static ReadWriteNBT createNBTObject() {
        return new NBTContainer();
    }

    public static ReadWriteNBT parseNBT(String nbtString) {
        return new NBTContainer(nbtString);
    }

    public static ReadWriteNBT readNBT(InputStream stream) {
        return new NBTContainer(stream);
    }

    public static ReadWriteNBT wrapNMSTag(Object nmsNbtTag) {
        return new NBTContainer(nmsNbtTag);
    }

    public static NBTFileHandle getFileHandle(File file) throws IOException {
        return new NBTFile(file);
    }

    public static ReadWriteNBT readFile(File file) throws IOException {
        return NBTFile.readFrom(file);
    }

    public static void writeFile(File file, ReadWriteNBT nbt) throws IOException {
        NBTFile.saveTo(file, (NBTCompound)nbt);
    }

    public static <T extends NBTProxy> T readNbt(ItemStack item, Class<T> wrapper) {
        return new ProxyBuilder<T>(new NBTItem(item, false, true, false), wrapper).readOnly().build();
    }

    public static <T extends NBTProxy> T readNbt(Entity entity, Class<T> wrapper) {
        return new ProxyBuilder<T>(new NBTEntity(entity, true), wrapper).readOnly().build();
    }

    public static <T extends NBTProxy> T readNbt(BlockState blockState, Class<T> wrapper) {
        return new ProxyBuilder<T>(new NBTTileEntity(blockState, true), wrapper).readOnly().build();
    }

    public static <T, X extends NBTProxy> T modify(ItemStack item, Class<X> wrapper, Function<X, T> function) {
        NBTItem nbti = new NBTItem(item, false, false, true);
        T val = function.apply(new ProxyBuilder<X>(nbti, wrapper).build());
        nbti.finalizeChanges();
        if (val instanceof ReadableNBT || val instanceof ReadableNBTList) {
            throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
        }
        nbti.setClosed();
        return val;
    }

    public static <X extends NBTProxy> void modify(ItemStack item, Class<X> wrapper, Consumer<X> consumer) {
        NBTItem nbti = new NBTItem(item, false, false, true);
        consumer.accept(new ProxyBuilder<X>(nbti, wrapper).build());
        nbti.finalizeChanges();
        nbti.setClosed();
    }

    public static <X extends NBTProxy> void modify(Entity entity, Class<X> wrapper, Consumer<X> consumer) {
        NBTEntity nbtEnt = new NBTEntity(entity);
        NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
        consumer.accept(new ProxyBuilder<X>(cont, wrapper).build());
        nbtEnt.setCompound(cont.getCompound());
        cont.setClosed();
    }

    public static <T, X extends NBTProxy> T modify(Entity entity, Class<X> wrapper, Function<X, T> function) {
        NBTEntity nbtEnt = new NBTEntity(entity);
        NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
        T val = function.apply(new ProxyBuilder<X>(cont, wrapper).build());
        nbtEnt.setCompound(cont.getCompound());
        cont.setClosed();
        return val;
    }

    public static <X extends NBTProxy> void modify(BlockState blockState, Class<X> wrapper, Consumer<X> consumer) {
        NBTTileEntity blockEnt = new NBTTileEntity(blockState);
        NBTContainer cont = new NBTContainer(blockEnt.getCompound());
        consumer.accept(new ProxyBuilder<X>(cont, wrapper).build());
        blockEnt.setCompound(cont);
        cont.setClosed();
    }

    public static <T, X extends NBTProxy> T modify(BlockState blockState, Class<X> wrapper, Function<X, T> function) {
        NBTTileEntity blockEnt = new NBTTileEntity(blockState);
        NBTContainer cont = new NBTContainer(blockEnt.getCompound());
        T val = function.apply(new ProxyBuilder<X>(cont, wrapper).build());
        blockEnt.setCompound(cont);
        cont.setClosed();
        return val;
    }
}

