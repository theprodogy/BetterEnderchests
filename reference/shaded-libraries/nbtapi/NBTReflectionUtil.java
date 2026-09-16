/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  org.bukkit.Bukkit
 *  org.bukkit.block.BlockState
 *  org.bukkit.entity.Entity
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTCompoundList;
import com.fernsehheft.enderchest.nbtapi.NBTContainer;
import com.fernsehheft.enderchest.nbtapi.NBTDoubleList;
import com.fernsehheft.enderchest.nbtapi.NBTFloatList;
import com.fernsehheft.enderchest.nbtapi.NBTIntArrayList;
import com.fernsehheft.enderchest.nbtapi.NBTIntegerList;
import com.fernsehheft.enderchest.nbtapi.NBTList;
import com.fernsehheft.enderchest.nbtapi.NBTListCompound;
import com.fernsehheft.enderchest.nbtapi.NBTLongList;
import com.fernsehheft.enderchest.nbtapi.NBTStringList;
import com.fernsehheft.enderchest.nbtapi.NBTType;
import com.fernsehheft.enderchest.nbtapi.NBTUUIDList;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.utils.DataFixerUtil;
import com.fernsehheft.enderchest.nbtapi.utils.GsonWrapper;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import com.fernsehheft.enderchest.nbtapi.utils.ReflectionUtil;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ClassWrapper;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.CodecHelper;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ObjectCreator;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NBTReflectionUtil {
    private static Field field_unhandledTags = null;
    private static Field field_handle = null;
    private static Object type_custom_data = null;
    private static Object registry_access = null;
    public static Codec<Object> itemstack_codec = null;
    public static DynamicOps<Object> nbtOps = null;
    public static DynamicOps<Object> nbtRegistryOps = null;
    public static Object problemReporter = null;
    private static final NBTContainer dummyNBT;

    private NBTReflectionUtil() {
    }

    public static Object getNMSEntity(Entity entity) {
        try {
            return ReflectionMethod.CRAFT_ENTITY_GET_HANDLE.run(ClassWrapper.CRAFT_ENTITY.getClazz().cast(entity), new Object[0]);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while getting the NMS Entity from a Bukkit Entity!", e);
        }
    }

    public static Object readNBT(InputStream stream) {
        try {
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R3)) {
                return ReflectionMethod.NBTFILE_READV2.run(null, stream, ReflectionMethod.NBTACCOUNTER_CREATE_UNLIMITED.run(null, new Object[0]));
            }
            return ReflectionMethod.NBTFILE_READ.run(null, stream);
        }
        catch (Exception e) {
            try {
                stream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            throw new NbtApiException("Exception while reading a NBT File!", e);
        }
    }

    public static Object writeNBT(Object nbt, OutputStream stream) {
        try {
            return ReflectionMethod.NBTFILE_WRITE.run(null, nbt, stream);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while writing NBT!", e);
        }
    }

    public static Object getCraftItemHandle(ItemStack item) {
        try {
            return field_handle.get(item);
        }
        catch (IllegalAccessException | IllegalArgumentException e) {
            throw new NbtApiException("Error getting handle from " + item.getClass(), e);
        }
    }

    public static void writeApiNBT(NBTCompound comp, OutputStream stream) {
        try {
            Object workingtag = comp.getResolvedObject();
            if (workingtag == null) {
                workingtag = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            }
            ReflectionMethod.NBTFILE_WRITE.run(null, workingtag, stream);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while writing NBT!", e);
        }
    }

    public static Object getItemRootNBTTagCompound(Object nmsitem) {
        try {
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                Object customData = ReflectionMethod.NMSDATACOMPONENTHOLDER_GET.run(nmsitem, type_custom_data);
                if (customData == null) {
                    return null;
                }
                return ReflectionMethod.NMSCUSTOMDATA_GETCOPY.run(customData, new Object[0]);
            }
            Object answer = ReflectionMethod.NMSITEM_GETTAG.run(nmsitem, new Object[0]);
            return answer;
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while getting an Itemstack's NBTCompound!", e);
        }
    }

    public static void setItemStackCompound(Object nmsItem, Object compound) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            if (compound == null) {
                ReflectionMethod.NMSITEM_SET.run(nmsItem, type_custom_data, null);
            } else {
                ReflectionMethod.NMSITEM_SET.run(nmsItem, type_custom_data, ObjectCreator.NMS_CUSTOMDATA.getInstance(compound));
            }
        } else {
            ReflectionMethod.ITEMSTACK_SET_TAG.run(nmsItem, compound);
        }
    }

    public static Object convertNBTCompoundtoNMSItem(NBTCompound nbtcompound) {
        Object nmsComp = null;
        try {
            nmsComp = NBTReflectionUtil.getToCompount(nbtcompound.getCompound(), nbtcompound);
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                if (nbtcompound.hasTag("DataVersion", NBTType.NBTTagInt)) {
                    int currentVersion;
                    int dataVersion = nbtcompound.getInteger("DataVersion");
                    if (dataVersion < (currentVersion = DataFixerUtil.getCurrentVersion())) {
                        nmsComp = DataFixerUtil.fixUpRawItemData(nmsComp, dataVersion, currentVersion);
                    }
                } else if (nbtcompound.hasTag("tag") || nbtcompound.hasTag("Count")) {
                    nmsComp = DataFixerUtil.fixUpRawItemData(nmsComp, 3700, DataFixerUtil.getCurrentVersion());
                }
                if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R5)) {
                    return CodecHelper.convertNbtToItemStack(nmsComp);
                }
                if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
                    Optional opt = (Optional)ReflectionMethod.NMSITEM_LOAD_MODERN.run(null, registry_access, nmsComp);
                    return opt.orElse(null);
                }
                return ReflectionMethod.NMSITEM_LOAD.run(null, registry_access, nmsComp);
            }
            if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_11_R1.getVersionId()) {
                return ObjectCreator.NMS_COMPOUNDFROMITEM.getInstance(nmsComp);
            }
            return ReflectionMethod.NMSITEM_CREATESTACK.run(null, nmsComp);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while converting NBTCompound to NMS ItemStack! " + nmsComp, e);
        }
    }

    public static NBTContainer convertNMSItemtoNBTCompound(Object nmsitem) {
        try {
            NBTContainer container;
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R5)) {
                container = new NBTContainer(CodecHelper.convertItemStackToNbt(nmsitem));
            } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                container = new NBTContainer(ReflectionMethod.NMSITEM_SAVE_MODERN.run(nmsitem, registry_access));
            } else {
                Object answer = ReflectionMethod.NMSITEM_SAVE.run(nmsitem, ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]));
                container = new NBTContainer(answer);
            }
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_12_R1)) {
                container.setInteger("DataVersion", DataFixerUtil.getCurrentVersion());
            }
            return container;
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while converting NMS ItemStack to NBTCompound!", e);
        }
    }

    @Deprecated
    public static Map<String, Object> getUnhandledNBTTags(ItemMeta meta) {
        try {
            return (Map)field_unhandledTags.get(meta);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while getting unhandled tags from ItemMeta!", e);
        }
    }

    public static Object getEntityNBTTagCompound(Object nmsEntity) {
        try {
            Object answer;
            Object nbt = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R5)) {
                Object output = ReflectionMethod.NMS_GET_TAG_VALUE_OUTPUT.run(null, problemReporter, registry_access);
                ReflectionMethod.NMS_ENTITY_GET_NBT_1216.run(nmsEntity, output);
                answer = ReflectionMethod.NMS_TAG_VALUE_OUTPUT_TO_TAG_COMPOUND.run(output, new Object[0]);
            } else {
                answer = ReflectionMethod.NMS_ENTITY_GET_NBT.run(nmsEntity, nbt);
            }
            if (answer == null) {
                answer = nbt;
            }
            return answer;
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while getting NBTCompound from NMS Entity!", e);
        }
    }

    public static Object setEntityNBTTag(Object nbtTag, Object nmsEntity) {
        try {
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R5)) {
                Object valueInputTag = ReflectionMethod.NMS_GET_TAG_VALUE_INPUT.run(null, problemReporter, registry_access, nbtTag);
                ReflectionMethod.NMS_ENTITY_SET_NBT_1216.run(nmsEntity, valueInputTag);
            } else {
                ReflectionMethod.NMS_ENTITY_SET_NBT.run(nmsEntity, nbtTag);
            }
            return nmsEntity;
        }
        catch (Exception ex) {
            throw new NbtApiException("Exception while setting the NBTCompound of an Entity", ex);
        }
    }

    public static Object getTileEntityNBTTagCompound(BlockState tile) {
        try {
            Object cworld = ClassWrapper.CRAFT_WORLD.getClazz().cast(tile.getWorld());
            Object nmsworld = ReflectionMethod.CRAFT_WORLD_GET_HANDLE.run(cworld, new Object[0]);
            Object o = null;
            if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
                o = ReflectionMethod.NMS_WORLD_GET_TILEENTITY_1_7_10.run(nmsworld, tile.getX(), tile.getY(), tile.getZ());
            } else {
                Object pos = ObjectCreator.NMS_BLOCKPOSITION.getInstance(tile.getX(), tile.getY(), tile.getZ());
                o = ReflectionMethod.NMS_WORLD_GET_TILEENTITY.run(nmsworld, pos);
            }
            if (o == null) {
                throw new NbtApiException("The passed BlockState(" + tile.getType() + ") doesn't point to a BlockEntity. Only BlockEntities like Chest/Signs/Furnance/etc have NBT.");
            }
            Object answer = null;
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R5)) {
                Object output = ReflectionMethod.NMS_GET_TAG_VALUE_OUTPUT.run(null, problemReporter, registry_access);
                ReflectionMethod.TILEENTITY_GET_NBT_1216.run(o, output);
                answer = ReflectionMethod.NMS_TAG_VALUE_OUTPUT_TO_TAG_COMPOUND.run(output, new Object[0]);
            } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                answer = ReflectionMethod.TILEENTITY_GET_NBT_1205.run(o, registry_access);
            } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_18_R1)) {
                answer = ReflectionMethod.TILEENTITY_GET_NBT_1181.run(o, new Object[0]);
            } else {
                answer = ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance();
                ReflectionMethod.TILEENTITY_GET_NBT.run(o, answer);
            }
            if (answer == null) {
                throw new NbtApiException("Unable to get NBTCompound from TileEntity! " + tile + " " + o);
            }
            return answer;
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while getting NBTCompound from TileEntity!", e);
        }
    }

    public static void setTileEntityNBTTagCompound(BlockState tile, Object comp) {
        try {
            Object cworld = ClassWrapper.CRAFT_WORLD.getClazz().cast(tile.getWorld());
            Object nmsworld = ReflectionMethod.CRAFT_WORLD_GET_HANDLE.run(cworld, new Object[0]);
            Object o = null;
            if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
                o = ReflectionMethod.NMS_WORLD_GET_TILEENTITY_1_7_10.run(nmsworld, tile.getX(), tile.getY(), tile.getZ());
            } else {
                Object pos = ObjectCreator.NMS_BLOCKPOSITION.getInstance(tile.getX(), tile.getY(), tile.getZ());
                o = ReflectionMethod.NMS_WORLD_GET_TILEENTITY.run(nmsworld, pos);
            }
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R5)) {
                Object valueInput = ReflectionMethod.NMS_GET_TAG_VALUE_INPUT.run(null, problemReporter, registry_access, comp);
                ReflectionMethod.TILEENTITY_SET_NBT_1216.run(o, valueInput);
            } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                ReflectionMethod.TILEENTITY_SET_NBT_1205.run(o, comp, registry_access);
            } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_17_R1)) {
                ReflectionMethod.TILEENTITY_SET_NBT.run(o, comp);
            } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1)) {
                Object blockData = ReflectionMethod.TILEENTITY_GET_BLOCKDATA.run(o, new Object[0]);
                ReflectionMethod.TILEENTITY_SET_NBT_LEGACY1161.run(o, blockData, comp);
            } else {
                ReflectionMethod.TILEENTITY_SET_NBT_LEGACY1151.run(o, comp);
            }
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while setting NBTData for a TileEntity!", e);
        }
    }

    public static Object getSubNBTTagCompound(Object compound, String name) {
        try {
            if (((Boolean)ReflectionMethod.COMPOUND_HAS_KEY.run(compound, name)).booleanValue()) {
                Object comp = ReflectionMethod.COMPOUND_GET_COMPOUND.run(compound, name);
                if (comp instanceof Optional) {
                    return ((Optional)comp).orElse(null);
                }
                return comp;
            }
            throw new NbtApiException("Tried getting invalid compound '" + name + "' from '" + compound + "'!");
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while getting NBT subcompounds!", e);
        }
    }

    public static void addNBTTagCompound(NBTCompound comp, String name) {
        if (name == null) {
            NBTReflectionUtil.remove(comp, name);
            return;
        }
        Object nbttag = comp.getCompound();
        if (nbttag == null) {
            nbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }
        if (!NBTReflectionUtil.validCompound(comp)) {
            return;
        }
        Object workingtag = NBTReflectionUtil.getToCompount(nbttag, comp);
        try {
            ReflectionMethod.COMPOUND_SET.run(workingtag, name, ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().newInstance());
            comp.setCompound(nbttag);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while adding a Compound!", e);
        }
    }

    public static boolean validCompound(NBTCompound comp) {
        Object root = comp.getCompound();
        if (root instanceof Optional) {
            root = ((Optional)root).orElse(null);
        }
        if (root == null) {
            root = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }
        Object tmp = NBTReflectionUtil.getToCompount(root, comp);
        comp.setResolvedObject(tmp);
        return tmp != null;
    }

    public static Object getToCompount(Object nbttag, NBTCompound comp) {
        ArrayDeque<String> structure = new ArrayDeque<String>();
        while (comp.getParent() != null) {
            structure.add(comp.getName());
            comp = comp.getParent();
        }
        if (nbttag instanceof Optional) {
            nbttag = ((Optional)nbttag).orElse(null);
        }
        while (!structure.isEmpty()) {
            String target = (String)structure.pollLast();
            if ((nbttag = NBTReflectionUtil.getSubNBTTagCompound(nbttag, target)) instanceof Optional) {
                nbttag = ((Optional)nbttag).orElse(null);
            }
            if (nbttag != null) continue;
            throw new NbtApiException("Unable to find tag '" + target + "' in " + nbttag);
        }
        return nbttag;
    }

    public static void mergeOtherNBTCompound(NBTCompound comp, NBTCompound nbtcompoundSrc) {
        Object workingtagSrc = nbtcompoundSrc.getResolvedObject();
        if (workingtagSrc == null) {
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }
        if (!NBTReflectionUtil.validCompound(comp)) {
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        }
        Object workingtag = NBTReflectionUtil.getToCompount(rootnbttag, comp);
        try {
            ReflectionMethod.COMPOUND_MERGE.run(workingtag, workingtagSrc);
            comp.setCompound(rootnbttag);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while merging two NBTCompounds!", e);
        }
    }

    public static void set(NBTCompound comp, String key, Object val) {
        if (val == null) {
            NBTReflectionUtil.remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }
        if (!NBTReflectionUtil.validCompound(comp)) {
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        }
        Object workingtag = NBTReflectionUtil.getToCompount(rootnbttag, comp);
        try {
            ReflectionMethod.COMPOUND_SET.run(workingtag, key, val);
            comp.setCompound(rootnbttag);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while setting key '" + key + "' to '" + val + "'!", e);
        }
    }

    public static <T> NBTList<T> getList(NBTCompound comp, String key, NBTType type, Class<T> clazz) {
        Object workingtag = comp.getResolvedObject();
        if (workingtag == null) {
            workingtag = dummyNBT.getCompound();
        }
        try {
            Object nbt = null;
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
                nbt = ReflectionMethod.COMPOUND_GET_LIST.run(workingtag, key);
                if (nbt instanceof Optional) {
                    nbt = ((Optional)nbt).orElse(null);
                }
                if (nbt == null) {
                    nbt = ClassWrapper.NMS_NBTTAGLIST.getClazz().newInstance();
                }
            } else {
                nbt = ReflectionMethod.COMPOUND_GET_LIST_LEGACY.run(workingtag, key, type.getId());
            }
            if (clazz == String.class) {
                return new NBTStringList(comp, key, type, nbt);
            }
            if (clazz == NBTListCompound.class) {
                return new NBTCompoundList(comp, key, type, nbt);
            }
            if (clazz == Integer.class) {
                return new NBTIntegerList(comp, key, type, nbt);
            }
            if (clazz == Float.class) {
                return new NBTFloatList(comp, key, type, nbt);
            }
            if (clazz == Double.class) {
                return new NBTDoubleList(comp, key, type, nbt);
            }
            if (clazz == Long.class) {
                return new NBTLongList(comp, key, type, nbt);
            }
            if (clazz == int[].class) {
                return new NBTIntArrayList(comp, key, type, nbt);
            }
            if (clazz == UUID.class) {
                return new NBTUUIDList(comp, key, type, nbt);
            }
            return null;
        }
        catch (Exception ex) {
            throw new NbtApiException("Exception while getting a list with the type '" + (Object)((Object)type) + "'!", ex);
        }
    }

    public static NBTType getListType(NBTCompound comp, String key) {
        Object workingtag = comp.getResolvedObject();
        if (workingtag == null) {
            workingtag = dummyNBT.getCompound();
        }
        try {
            Field f;
            Object nbt = ReflectionMethod.COMPOUND_GET.run(workingtag, key);
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
                if (nbt instanceof Optional) {
                    nbt = ((Optional)nbt).orElse(null);
                }
                if (nbt == null) {
                    return NBTType.NBTTagEnd;
                }
                if (new NBTStringList(comp, key, NBTType.NBTTagString, nbt).isEmpty()) {
                    return NBTType.NBTTagEnd;
                }
                Object compound = ReflectionMethod.LIST_GET.run(nbt, 0);
                if (compound instanceof Optional) {
                    compound = ((Optional)compound).orElse(null);
                }
                if (compound == null) {
                    return NBTType.NBTTagEnd;
                }
                return NBTType.fromName((String)ReflectionMethod.TAGTYPE_GET_NAME.run(ReflectionMethod.TAGTYPE_OWN_TYPE.run(compound, new Object[0]), new Object[0]));
            }
            String fieldname = "type";
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_17_R1)) {
                fieldname = "w";
            }
            try {
                f = nbt.getClass().getDeclaredField(fieldname);
            }
            catch (NoSuchFieldException ignore) {
                f = nbt.getClass().getDeclaredField("type");
            }
            f.setAccessible(true);
            return NBTType.valueOf(f.getByte(nbt));
        }
        catch (Exception ex) {
            throw new NbtApiException("Exception while getting the list type!", ex);
        }
    }

    public static Object getEntry(NBTCompound comp, String key) {
        Object workingtag = comp.getResolvedObject();
        try {
            return ReflectionMethod.COMPOUND_GET.run(workingtag, key);
        }
        catch (Exception ex) {
            throw new NbtApiException("Exception while getting an Entry!", ex);
        }
    }

    public static void setObject(NBTCompound comp, String key, Object value) {
        if (!MinecraftVersion.hasGsonSupport()) {
            return;
        }
        try {
            String json = GsonWrapper.getString(value);
            NBTReflectionUtil.setData(comp, ReflectionMethod.COMPOUND_SET_STRING, key, json);
        }
        catch (Exception e) {
            throw new NbtApiException("Exception while setting the Object '" + value + "'!", e);
        }
    }

    public static <T> T getObject(NBTCompound comp, String key, Class<T> type) {
        if (!MinecraftVersion.hasGsonSupport()) {
            return null;
        }
        String json = (String)NBTReflectionUtil.getData(comp, ReflectionMethod.COMPOUND_GET_STRING, key);
        if (json == null) {
            return null;
        }
        return GsonWrapper.deserializeJson(json, type);
    }

    public static void remove(NBTCompound comp, String key) {
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            return;
        }
        if (!NBTReflectionUtil.validCompound(comp)) {
            return;
        }
        Object workingtag = NBTReflectionUtil.getToCompount(rootnbttag, comp);
        ReflectionMethod.COMPOUND_REMOVE_KEY.run(workingtag, key);
        comp.setCompound(rootnbttag);
    }

    public static Set<String> getKeys(NBTCompound comp) {
        Object workingtag = comp.getResolvedObject();
        if (workingtag == null) {
            return Collections.emptySet();
        }
        return (Set)ReflectionMethod.COMPOUND_GET_KEYS.run(workingtag, new Object[0]);
    }

    public static void setData(NBTCompound comp, ReflectionMethod type, String key, Object data) {
        if (data == null) {
            NBTReflectionUtil.remove(comp, key);
            return;
        }
        Object rootnbttag = comp.getCompound();
        if (rootnbttag == null) {
            rootnbttag = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }
        if (!NBTReflectionUtil.validCompound(comp)) {
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        }
        Object workingtag = NBTReflectionUtil.getToCompount(rootnbttag, comp);
        type.run(workingtag, key, data);
        comp.setCompound(rootnbttag);
    }

    public static Object getData(NBTCompound comp, ReflectionMethod type, String key) {
        Object obj;
        Object workingtag = comp.getResolvedObject();
        if (workingtag == null) {
            workingtag = dummyNBT.getCompound();
        }
        if (workingtag instanceof Optional) {
            workingtag = ((Optional)workingtag).orElseGet(() -> dummyNBT.getCompound());
        }
        if ((obj = type.run(workingtag, key)) instanceof Optional) {
            return ((Optional)obj).orElseGet(() -> NBTReflectionUtil.getDefaultValue(type));
        }
        return obj;
    }

    private static Object getDefaultValue(ReflectionMethod type) {
        if (type == ReflectionMethod.COMPOUND_GET_STRING) {
            return "";
        }
        if (type == ReflectionMethod.COMPOUND_GET_BYTE) {
            return (byte)0;
        }
        if (type == ReflectionMethod.COMPOUND_GET_SHORT) {
            return (short)0;
        }
        if (type == ReflectionMethod.COMPOUND_GET_BOOLEAN) {
            return false;
        }
        if (type == ReflectionMethod.COMPOUND_GET_INT) {
            return 0;
        }
        if (type == ReflectionMethod.COMPOUND_GET_LONG) {
            return 0L;
        }
        if (type == ReflectionMethod.COMPOUND_GET_FLOAT) {
            return Float.valueOf(0.0f);
        }
        if (type == ReflectionMethod.COMPOUND_GET_DOUBLE) {
            return 0.0;
        }
        if (type == ReflectionMethod.COMPOUND_GET_BYTEARRAY) {
            return new byte[0];
        }
        if (type == ReflectionMethod.COMPOUND_GET_INTARRAY) {
            return new int[0];
        }
        if (type == ReflectionMethod.COMPOUND_GET_LONGARRAY) {
            return new long[0];
        }
        return null;
    }

    static {
        try {
            field_unhandledTags = ClassWrapper.CRAFT_METAITEM.getClazz().getDeclaredField("unhandledTags");
            field_unhandledTags.setAccessible(true);
        }
        catch (NoSuchFieldException noSuchFieldException) {
            // empty catch block
        }
        try {
            field_handle = ClassWrapper.CRAFT_ITEMSTACK.getClazz().getDeclaredField("handle");
            field_handle.setAccessible(true);
        }
        catch (NoSuchFieldException noSuchFieldException) {
            // empty catch block
        }
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            try {
                Field typeField = ReflectionUtil.getMappedField(ClassWrapper.NMS_DATACOMPONENTS.getClazz(), "net.minecraft.core.component.DataComponents#CUSTOM_DATA");
                type_custom_data = typeField.get(null);
            }
            catch (Exception e) {
                MinecraftVersion.getLogger().log(Level.WARNING, "Unable to find DataComponents#CUSTOM_DATA, NBTApi will not be able to read/write custom data on 1.20+", e);
            }
            try {
                Object nmsServer = ReflectionMethod.NMSSERVER_GETSERVER.run(Bukkit.getServer(), new Object[0]);
                registry_access = ReflectionMethod.NMSSERVER_GETREGISTRYACCESS.run(nmsServer, new Object[0]);
                itemstack_codec = (Codec)ReflectionUtil.getMappedField(ClassWrapper.NMS_ITEMSTACK.getClazz(), "net.minecraft.world.item.ItemStack#CODEC").get(null);
                nbtOps = (DynamicOps)ReflectionUtil.getMappedField(ClassWrapper.NMS_NBTOPS.getClazz(), "net.minecraft.nbt.NbtOps#INSTANCE").get(null);
                if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R5)) {
                    nbtRegistryOps = (DynamicOps)ReflectionMethod.GET_SERIALIZATION_CONTEXT.run(registry_access, nbtOps);
                    problemReporter = ReflectionUtil.getMappedField(ClassWrapper.NMS_PROBLEM_REPORTER.getClazz(), "net.minecraft.util.ProblemReporter#DISCARDING").get(null);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        dummyNBT = new NBTContainer();
    }
}

