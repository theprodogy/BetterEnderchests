/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.ItemStack
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompoundList;
import com.fernsehheft.enderchest.nbtapi.NBTContainer;
import com.fernsehheft.enderchest.nbtapi.NBTItem;
import com.fernsehheft.enderchest.nbtapi.NBTList;
import com.fernsehheft.enderchest.nbtapi.NBTListCompound;
import com.fernsehheft.enderchest.nbtapi.NBTReflectionUtil;
import com.fernsehheft.enderchest.nbtapi.NBTType;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.iface.NBTHandler;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBTList;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBT;
import com.fernsehheft.enderchest.nbtapi.utils.CheckUtil;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import com.fernsehheft.enderchest.nbtapi.utils.PathUtil;
import com.fernsehheft.enderchest.nbtapi.utils.UUIDUtil;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.Forge1710Mappings;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bukkit.inventory.ItemStack;

public class NBTCompound
implements ReadWriteNBT {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = this.readWriteLock.readLock();
    private final Lock writeLock = this.readWriteLock.writeLock();
    private String compundName;
    private NBTCompound parent;
    private final boolean readOnly;
    private Object readOnlyCache;

    protected NBTCompound(NBTCompound owner, String name) {
        this(owner, name, false);
    }

    protected NBTCompound(NBTCompound owner, String name, boolean readOnly) {
        this.compundName = name;
        this.parent = owner;
        this.readOnly = readOnly;
    }

    protected Lock getReadLock() {
        return this.readLock;
    }

    protected Lock getWriteLock() {
        return this.writeLock;
    }

    protected void saveCompound() {
        if (this.parent != null) {
            this.parent.saveCompound();
        }
    }

    protected void setResolvedObject(Object object) {
        if (this.isClosed()) {
            throw new NbtApiException("Tried using closed NBT data!");
        }
        if (this.readOnly) {
            this.readOnlyCache = object;
        }
    }

    protected void setClosed() {
        if (this.parent != null) {
            this.parent.setClosed();
        }
    }

    protected boolean isClosed() {
        if (this.parent != null) {
            return this.parent.isClosed();
        }
        return false;
    }

    protected boolean isReadOnly() {
        return this.readOnly;
    }

    protected Object getResolvedObject() {
        if (this.isClosed()) {
            throw new NbtApiException("Tried using closed NBT data!");
        }
        if (this.readOnlyCache != null) {
            return this.readOnlyCache;
        }
        Object rootnbttag = this.getCompound();
        if (rootnbttag instanceof Optional) {
            rootnbttag = ((Optional)rootnbttag).orElse(null);
        }
        if (rootnbttag == null) {
            return null;
        }
        if (!NBTReflectionUtil.validCompound(this)) {
            throw new NbtApiException("The Compound wasn't able to be linked back to the root!");
        }
        Object workingtag = NBTReflectionUtil.getToCompount(rootnbttag, this);
        if (this.readOnly) {
            this.readOnlyCache = workingtag;
        }
        return workingtag;
    }

    public String getName() {
        return this.compundName;
    }

    public Object getCompound() {
        return this.parent.getCompound();
    }

    protected void setCompound(Object compound) {
        this.parent.setCompound(compound);
    }

    public NBTCompound getParent() {
        return this.parent;
    }

    public void mergeCompound(NBTCompound comp) {
        if (comp == null) {
            return;
        }
        try {
            this.writeLock.lock();
            NBTReflectionUtil.mergeOtherNBTCompound(this, comp);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void mergeCompound(ReadableNBT comp) {
        if (!(comp instanceof NBTCompound)) {
            throw new NbtApiException("Unknown NBT object: " + comp);
        }
        this.mergeCompound((NBTCompound)comp);
    }

    @Override
    public void setString(String key, String value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_STRING, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public String getString(String key) {
        try {
            this.readLock.lock();
            String string = (String)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_STRING, key);
            return string;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setInteger(String key, Integer value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_INT, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public Integer getInteger(String key) {
        try {
            this.readLock.lock();
            Integer n = (Integer)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_INT, key);
            return n;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setDouble(String key, Double value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_DOUBLE, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public Double getDouble(String key) {
        try {
            this.readLock.lock();
            Double d = (Double)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_DOUBLE, key);
            return d;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setByte(String key, Byte value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BYTE, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public Byte getByte(String key) {
        try {
            this.readLock.lock();
            Byte by = (Byte)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BYTE, key);
            return by;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setShort(String key, Short value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_SHORT, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public Short getShort(String key) {
        try {
            this.readLock.lock();
            Short s = (Short)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_SHORT, key);
            return s;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setLong(String key, Long value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_LONG, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public Long getLong(String key) {
        try {
            this.readLock.lock();
            Long l = (Long)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_LONG, key);
            return l;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setFloat(String key, Float value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_FLOAT, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public Float getFloat(String key) {
        try {
            this.readLock.lock();
            Float f = (Float)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_FLOAT, key);
            return f;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setByteArray(String key, byte[] value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BYTEARRAY, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public byte[] getByteArray(String key) {
        try {
            this.readLock.lock();
            byte[] byArray = (byte[])NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BYTEARRAY, key);
            return byArray;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setIntArray(String key, int[] value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_INTARRAY, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public int[] getIntArray(String key) {
        try {
            this.readLock.lock();
            int[] nArray = (int[])NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_INTARRAY, key);
            return nArray;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setLongArray(String key, long[] value) {
        CheckUtil.assertAvailable(MinecraftVersion.MC1_16_R1);
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_LONGARRAY, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public long[] getLongArray(String key) {
        CheckUtil.assertAvailable(MinecraftVersion.MC1_16_R1);
        try {
            this.readLock.lock();
            long[] lArray = (long[])NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_LONGARRAY, key);
            return lArray;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setBoolean(String key, Boolean value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_BOOLEAN, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    protected void set(String key, Object val) {
        NBTReflectionUtil.set(this, key, val);
        this.saveCompound();
    }

    @Override
    public Boolean getBoolean(String key) {
        try {
            this.readLock.lock();
            Boolean bl = (Boolean)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_BOOLEAN, key);
            return bl;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Deprecated
    public void setObject(String key, Object value) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.setObject(this, key, value);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Deprecated
    public <T> T getObject(String key, Class<T> type) {
        try {
            this.readLock.lock();
            T t = NBTReflectionUtil.getObject(this, key, type);
            return t;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setItemStack(String key, ItemStack item) {
        try {
            this.writeLock.lock();
            this.removeKey(key);
            this.addCompound(key).mergeCompound(NBTItem.convertItemtoNBT(item));
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ItemStack getItemStack(String key) {
        try {
            this.readLock.lock();
            NBTCompound comp = this.getCompound(key);
            if (comp == null) {
                ItemStack itemStack = null;
                return itemStack;
            }
            ItemStack itemStack = NBTItem.convertNBTtoItem(comp);
            return itemStack;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setItemStackArray(String key, ItemStack[] items) {
        try {
            this.writeLock.lock();
            this.removeKey(key);
            this.addCompound(key).mergeCompound(NBTItem.convertItemArraytoNBT(items));
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ItemStack[] getItemStackArray(String key) {
        try {
            this.readLock.lock();
            NBTCompound comp = this.getCompound(key);
            if (comp == null) {
                ItemStack[] itemStackArray = null;
                return itemStackArray;
            }
            ItemStack[] itemStackArray = NBTItem.convertNBTtoItemArray(comp);
            return itemStackArray;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void setUUID(String key, UUID value) {
        try {
            this.writeLock.lock();
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
                this.setIntArray(key, UUIDUtil.uuidToIntArray(value));
            } else if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1)) {
                NBTReflectionUtil.setData(this, ReflectionMethod.COMPOUND_SET_UUID, key, value);
            } else {
                this.setString(key, value.toString());
            }
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public UUID getUUID(String key) {
        try {
            this.readLock.lock();
            NBTType type = this.getType(key);
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4) && type == NBTType.NBTTagIntArray) {
                return UUIDUtil.uuidFromIntArray(this.getIntArray(key));
            }
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1) && type == NBTType.NBTTagIntArray) {
                return (UUID)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_UUID, key);
            }
            if (type == NBTType.NBTTagString) {
                try {
                    return UUID.fromString(this.getString(key));
                }
                catch (IllegalArgumentException ex) {
                    return null;
                }
            }
            return null;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Deprecated
    public Boolean hasKey(String key) {
        return this.hasTag(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean hasTag(String key) {
        try {
            this.readLock.lock();
            Boolean b = (Boolean)NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_HAS_KEY, key);
            if (b == null) {
                boolean bl = false;
                return bl;
            }
            boolean bl = b;
            return bl;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void removeKey(String key) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.remove(this, key);
            this.saveCompound();
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public Set<String> getKeys() {
        try {
            this.readLock.lock();
            HashSet<String> hashSet = new HashSet<String>(NBTReflectionUtil.getKeys(this));
            return hashSet;
        }
        finally {
            this.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public NBTCompound addCompound(String name) {
        try {
            this.writeLock.lock();
            if (this.getType(name) == NBTType.NBTTagCompound) {
                NBTCompound nBTCompound = this.getCompound(name);
                return nBTCompound;
            }
            NBTReflectionUtil.addNBTTagCompound(this, name);
            NBTCompound comp = this.getCompound(name);
            if (comp == null) {
                throw new NbtApiException("Error while adding Compound, got null!");
            }
            this.saveCompound();
            NBTCompound nBTCompound = comp;
            return nBTCompound;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTCompound getCompound(String name) {
        try {
            this.readLock.lock();
            if (this.getType(name) != NBTType.NBTTagCompound) {
                NBTCompound nBTCompound = null;
                return nBTCompound;
            }
            NBTCompound next = new NBTCompound(this, name, this.readOnly);
            if (NBTReflectionUtil.validCompound(next)) {
                NBTCompound nBTCompound = next;
                return nBTCompound;
            }
            NBTCompound nBTCompound = null;
            return nBTCompound;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public NBTCompound getOrCreateCompound(String name) {
        return this.addCompound(name);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTList<String> getStringList(String name) {
        try {
            this.writeLock.lock();
            NBTList<String> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagString, String.class);
            this.saveCompound();
            NBTList<String> nBTList = list;
            return nBTList;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTList<Integer> getIntegerList(String name) {
        try {
            this.writeLock.lock();
            NBTList<Integer> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagInt, Integer.class);
            this.saveCompound();
            NBTList<Integer> nBTList = list;
            return nBTList;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTList<int[]> getIntArrayList(String name) {
        try {
            this.writeLock.lock();
            NBTList<int[]> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagIntArray, int[].class);
            this.saveCompound();
            NBTList<int[]> nBTList = list;
            return nBTList;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTList<UUID> getUUIDList(String name) {
        try {
            this.writeLock.lock();
            NBTList<UUID> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagIntArray, UUID.class);
            this.saveCompound();
            NBTList<UUID> nBTList = list;
            return nBTList;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTList<Float> getFloatList(String name) {
        try {
            this.writeLock.lock();
            NBTList<Float> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagFloat, Float.class);
            this.saveCompound();
            NBTList<Float> nBTList = list;
            return nBTList;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTList<Double> getDoubleList(String name) {
        try {
            this.writeLock.lock();
            NBTList<Double> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagDouble, Double.class);
            this.saveCompound();
            NBTList<Double> nBTList = list;
            return nBTList;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTList<Long> getLongList(String name) {
        try {
            this.writeLock.lock();
            NBTList<Long> list = NBTReflectionUtil.getList(this, name, NBTType.NBTTagLong, Long.class);
            this.saveCompound();
            NBTList<Long> nBTList = list;
            return nBTList;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public NBTType getListType(String name) {
        try {
            this.readLock.lock();
            if (this.getType(name) != NBTType.NBTTagList) {
                NBTType nBTType = null;
                return nBTType;
            }
            NBTType nBTType = NBTReflectionUtil.getListType(this, name);
            return nBTType;
        }
        finally {
            this.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTCompoundList getCompoundList(String name) {
        try {
            this.writeLock.lock();
            NBTCompoundList list = (NBTCompoundList)NBTReflectionUtil.getList(this, name, NBTType.NBTTagCompound, NBTListCompound.class);
            this.saveCompound();
            NBTCompoundList nBTCompoundList = list;
            return nBTCompoundList;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public <T> T getOrDefault(String key, T defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("Default type in getOrDefault can't be null!");
        }
        if (!this.hasTag(key)) {
            return defaultValue;
        }
        Class<?> clazz = defaultValue.getClass();
        if (clazz == Boolean.class || clazz == Boolean.TYPE) {
            return (T)this.getBoolean(key);
        }
        if (clazz == Byte.class || clazz == Byte.TYPE) {
            return (T)this.getByte(key);
        }
        if (clazz == Short.class || clazz == Short.TYPE) {
            return (T)this.getShort(key);
        }
        if (clazz == Integer.class || clazz == Integer.TYPE) {
            return (T)this.getInteger(key);
        }
        if (clazz == Long.class || clazz == Long.TYPE) {
            return (T)this.getLong(key);
        }
        if (clazz == Float.class || clazz == Float.TYPE) {
            return (T)this.getFloat(key);
        }
        if (clazz == Double.class || clazz == Double.TYPE) {
            return (T)this.getDouble(key);
        }
        if (clazz == byte[].class) {
            return (T)this.getByteArray(key);
        }
        if (clazz == int[].class) {
            return (T)this.getIntArray(key);
        }
        if (clazz == long[].class) {
            return (T)this.getLongArray(key);
        }
        if (clazz == String.class) {
            return (T)this.getString(key);
        }
        if (clazz == UUID.class) {
            UUID uuid = this.getUUID(key);
            return (T)(uuid == null ? defaultValue : uuid);
        }
        if (clazz.isEnum()) {
            Object obj = this.getEnum(key, defaultValue.getClass());
            return (T)(obj == null ? defaultValue : obj);
        }
        throw new NbtApiException("Unsupported type for getOrDefault: " + clazz.getName());
    }

    @Override
    public <T> T getOrNull(String key, Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Default type in getOrNull can't be null!");
        }
        if (!this.hasTag(key)) {
            return null;
        }
        if (type == Boolean.class || type == Boolean.TYPE) {
            return (T)this.getBoolean(key);
        }
        if (type == Byte.class || type == Byte.TYPE) {
            return (T)this.getByte(key);
        }
        if (type == Short.class || type == Short.TYPE) {
            return (T)this.getShort(key);
        }
        if (type == Integer.class || type == Integer.TYPE) {
            return (T)this.getInteger(key);
        }
        if (type == Long.class || type == Long.TYPE) {
            return (T)this.getLong(key);
        }
        if (type == Float.class || type == Float.TYPE) {
            return (T)this.getFloat(key);
        }
        if (type == Double.class || type == Double.TYPE) {
            return (T)this.getDouble(key);
        }
        if (type == byte[].class) {
            return (T)this.getByteArray(key);
        }
        if (type == int[].class) {
            return (T)this.getIntArray(key);
        }
        if (type == long[].class) {
            return (T)this.getLongArray(key);
        }
        if (type == String.class) {
            return (T)this.getString(key);
        }
        if (type == UUID.class) {
            return (T)this.getUUID(key);
        }
        if (type.isEnum()) {
            return (T)this.getEnum(key, type);
        }
        throw new NbtApiException("Unsupported type for getOrNull: " + type.getName());
    }

    @Override
    public <T> T resolveOrNull(String key, Class<?> type) {
        List<PathUtil.PathSegment> keys = PathUtil.splitPath(key);
        NBTCompound tag = this;
        for (int i = 0; i < keys.size() - 1; ++i) {
            PathUtil.PathSegment segment = keys.get(i);
            if (!segment.hasIndex()) {
                if ((tag = tag.getCompound(segment.getPath())) != null) continue;
                return null;
            }
            if (tag.getType(segment.getPath()) != NBTType.NBTTagList || tag.getListType(segment.getPath()) != NBTType.NBTTagCompound) continue;
            NBTCompoundList list = tag.getCompoundList(segment.getPath());
            tag = segment.getIndex() >= 0 ? list.get(segment.getIndex()) : list.get(list.size() + segment.getIndex());
        }
        PathUtil.PathSegment segment = keys.get(keys.size() - 1);
        if (!segment.hasIndex()) {
            return tag.getOrNull(segment.getPath(), type);
        }
        return (T)this.getIndexedValue(tag, segment, type);
    }

    @Override
    public <T> T resolveOrDefault(String key, T defaultValue) {
        List<PathUtil.PathSegment> keys = PathUtil.splitPath(key);
        NBTCompound tag = this;
        for (int i = 0; i < keys.size() - 1; ++i) {
            PathUtil.PathSegment segment = keys.get(i);
            if (!segment.hasIndex()) {
                if ((tag = tag.getCompound(segment.getPath())) != null) continue;
                return defaultValue;
            }
            if (tag.getType(segment.getPath()) != NBTType.NBTTagList || tag.getListType(segment.getPath()) != NBTType.NBTTagCompound) continue;
            NBTCompoundList list = tag.getCompoundList(segment.getPath());
            tag = segment.getIndex() >= 0 ? list.get(segment.getIndex()) : list.get(list.size() + segment.getIndex());
        }
        PathUtil.PathSegment segment = keys.get(keys.size() - 1);
        if (!segment.hasIndex()) {
            return tag.getOrDefault(segment.getPath(), defaultValue);
        }
        return (T)this.getIndexedValue(tag, segment, defaultValue.getClass());
    }

    private <T> T getIndexedValue(NBTCompound comp, PathUtil.PathSegment segment, Class<T> type) {
        if (type == String.class) {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagList && comp.getListType(segment.getPath()) == NBTType.NBTTagString) {
                if (segment.getIndex() >= 0) {
                    return (T)comp.getStringList(segment.getPath()).get(segment.getIndex());
                }
                ReadWriteNBTList list = comp.getStringList(segment.getPath());
                return (T)list.get(list.size() + segment.getIndex());
            }
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
        }
        if (type == Integer.TYPE || type == Integer.class) {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagList && comp.getListType(segment.getPath()) == NBTType.NBTTagInt) {
                if (segment.getIndex() >= 0) {
                    return (T)comp.getIntegerList(segment.getPath()).get(segment.getIndex());
                }
                ReadWriteNBTList list = comp.getIntegerList(segment.getPath());
                return (T)list.get(list.size() + segment.getIndex());
            }
            if (comp.getType(segment.getPath()) == NBTType.NBTTagIntArray) {
                if (segment.getIndex() >= 0) {
                    int[] array = comp.getIntArray(segment.getPath());
                    if (array != null) {
                        return (T)Integer.valueOf(array[segment.getIndex()]);
                    }
                } else {
                    int[] array = comp.getIntArray(segment.getPath());
                    if (array != null) {
                        return (T)Integer.valueOf(array[array.length + segment.getIndex()]);
                    }
                }
            }
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
        }
        if (type == Long.TYPE || type == Long.class) {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagList && comp.getListType(segment.getPath()) == NBTType.NBTTagLong) {
                if (segment.getIndex() >= 0) {
                    return (T)comp.getLongList(segment.getPath()).get(segment.getIndex());
                }
                ReadWriteNBTList list = comp.getLongList(segment.getPath());
                return (T)list.get(list.size() + segment.getIndex());
            }
            if (comp.getType(segment.getPath()) == NBTType.NBTTagLongArray) {
                if (segment.getIndex() >= 0) {
                    long[] array = comp.getLongArray(segment.getPath());
                    if (array != null) {
                        return (T)Long.valueOf(array[segment.getIndex()]);
                    }
                } else {
                    long[] array = comp.getLongArray(segment.getPath());
                    if (array != null) {
                        return (T)Long.valueOf(array[array.length + segment.getIndex()]);
                    }
                }
            }
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
        }
        if (type == Float.TYPE || type == Float.class) {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagList && comp.getListType(segment.getPath()) == NBTType.NBTTagFloat) {
                if (segment.getIndex() >= 0) {
                    return (T)comp.getFloatList(segment.getPath()).get(segment.getIndex());
                }
                ReadWriteNBTList list = comp.getFloatList(segment.getPath());
                return (T)list.get(list.size() + segment.getIndex());
            }
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
        }
        if (type == Double.TYPE || type == Double.class) {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagList && comp.getListType(segment.getPath()) == NBTType.NBTTagDouble) {
                if (segment.getIndex() >= 0) {
                    return (T)comp.getDoubleList(segment.getPath()).get(segment.getIndex());
                }
                ReadWriteNBTList list = comp.getDoubleList(segment.getPath());
                return (T)list.get(list.size() + segment.getIndex());
            }
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
        }
        if (type == int[].class) {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagList && comp.getListType(segment.getPath()) == NBTType.NBTTagIntArray) {
                if (segment.getIndex() >= 0) {
                    return (T)comp.getIntArrayList(segment.getPath()).get(segment.getIndex());
                }
                ReadWriteNBTList list = comp.getIntArrayList(segment.getPath());
                return (T)list.get(list.size() + segment.getIndex());
            }
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
        }
        if (type == Byte.TYPE || type == Byte.class) {
            if (comp.getType(segment.getPath()) == NBTType.NBTTagByteArray) {
                if (segment.getIndex() >= 0) {
                    byte[] array = comp.getByteArray(segment.getPath());
                    if (array != null) {
                        return (T)Byte.valueOf(array[segment.getIndex()]);
                    }
                } else {
                    byte[] array = comp.getByteArray(segment.getPath());
                    if (array != null) {
                        return (T)Byte.valueOf(array[array.length + segment.getIndex()]);
                    }
                }
            }
            throw new NbtApiException("No fitting list/array found for " + segment.getPath() + " of type " + type);
        }
        throw new NbtApiException("Unable to get indexed value for type " + type);
    }

    @Override
    public ReadWriteNBT resolveCompound(String key) {
        List<PathUtil.PathSegment> keys = PathUtil.splitPath(key);
        NBTCompound tag = this;
        for (int i = 0; i < keys.size(); ++i) {
            PathUtil.PathSegment segment = keys.get(i);
            if (!segment.hasIndex()) {
                if ((tag = tag.getCompound(segment.getPath())) != null) continue;
                return null;
            }
            if (tag.getType(segment.getPath()) != NBTType.NBTTagList || tag.getListType(segment.getPath()) != NBTType.NBTTagCompound) continue;
            NBTCompoundList list = tag.getCompoundList(segment.getPath());
            tag = segment.getIndex() >= 0 ? list.get(segment.getIndex()) : list.get(list.size() + segment.getIndex());
        }
        return tag;
    }

    @Override
    public ReadWriteNBT resolveOrCreateCompound(String key) {
        List<PathUtil.PathSegment> keys = PathUtil.splitPath(key);
        NBTCompound tag = this;
        for (int i = 0; i < keys.size(); ++i) {
            PathUtil.PathSegment segment = keys.get(i);
            if (!segment.hasIndex()) {
                if ((tag = tag.getOrCreateCompound(segment.getPath())) != null) continue;
                return null;
            }
            if (tag.getType(segment.getPath()) != NBTType.NBTTagList || tag.getListType(segment.getPath()) != NBTType.NBTTagCompound) continue;
            NBTCompoundList list = tag.getCompoundList(segment.getPath());
            tag = segment.getIndex() >= 0 ? list.get(segment.getIndex()) : list.get(list.size() + segment.getIndex());
        }
        return tag;
    }

    @Override
    public <E extends Enum<?>> void setEnum(String key, E value) {
        if (value == null) {
            this.removeKey(key);
            return;
        }
        this.setString(key, value.name());
    }

    @Override
    public <E extends Enum<E>> E getEnum(String key, Class<E> type) {
        if (key == null || type == null) {
            return null;
        }
        String name = this.getString(key);
        if (name == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, name);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public NBTType getType(String name) {
        try {
            this.readLock.lock();
            if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
                Object nbtbase = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET, name);
                if (nbtbase == null) {
                    NBTType nBTType = null;
                    return nBTType;
                }
                NBTType nBTType = NBTType.valueOf(((Byte)ReflectionMethod.COMPOUND_OWN_TYPE_LEGACY.run(nbtbase, new Object[0])).byteValue());
                return nBTType;
            }
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
                Object o = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET, name);
                if (o == null) {
                    NBTType nBTType = null;
                    return nBTType;
                }
                NBTType nBTType = NBTType.fromName((String)ReflectionMethod.TAGTYPE_GET_NAME.run(ReflectionMethod.TAGTYPE_OWN_TYPE.run(o, new Object[0]), new Object[0]));
                return nBTType;
            }
            Object o = NBTReflectionUtil.getData(this, ReflectionMethod.COMPOUND_GET_TYPE, name);
            if (o == null) {
                NBTType nBTType = null;
                return nBTType;
            }
            NBTType nBTType = NBTType.valueOf(((Byte)o).byteValue());
            return nBTType;
        }
        finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void writeCompound(OutputStream stream) {
        try {
            this.writeLock.lock();
            NBTReflectionUtil.writeApiNBT(this, stream);
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public <T> T get(String key, NBTHandler<T> handler) {
        return handler.get(this, key);
    }

    @Override
    public <T> void set(String key, T value, NBTHandler<T> handler) {
        handler.set(this, key, value);
    }

    @Override
    public String toString() {
        return this.asNBTString();
    }

    @Deprecated
    public String toString(String key) {
        return this.asNBTString();
    }

    @Override
    public void clearNBT() {
        for (String key : this.getKeys()) {
            this.removeKey(key);
        }
    }

    @Deprecated
    public String asNBTString() {
        try {
            this.readLock.lock();
            Object comp = this.getResolvedObject();
            if (comp == null) {
                String string = "{}";
                return string;
            }
            if (MinecraftVersion.isForgePresent() && MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
                String string = Forge1710Mappings.toString(comp);
                return string;
            }
            String string = comp.toString();
            return string;
        }
        finally {
            this.readLock.unlock();
        }
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof NBTCompound) {
            NBTCompound other = (NBTCompound)obj;
            if (this.getKeys().equals(other.getKeys())) {
                for (String key : this.getKeys()) {
                    if (NBTCompound.isEqual(this, other, key)) continue;
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public NBTCompound extractDifference(ReadableNBT other) {
        if (this == other) {
            return new NBTContainer();
        }
        if (other instanceof NBTCompound) {
            return NBTCompound.saveDiff(new NBTContainer(), this, (NBTCompound)other);
        }
        throw new NbtApiException("Unknown NBT object: " + other);
    }

    private static NBTCompound saveDiff(NBTCompound saveTo, NBTCompound compA, NBTCompound compB) {
        for (String key : compA.getKeys()) {
            NBTCompound.saveDiff(saveTo, compA, compB, key);
        }
        return saveTo;
    }

    private static void saveDiff(NBTCompound saveTo, NBTCompound compA, NBTCompound compB, String key) {
        boolean typeMismatch = compA.getType(key) != compB.getType(key);
        switch (compA.getType(key)) {
            case NBTTagByte: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setByte(key, compA.getByte(key));
                }
                return;
            }
            case NBTTagByteArray: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setByteArray(key, compA.getByteArray(key));
                }
                return;
            }
            case NBTTagCompound: {
                NBTCompound tmp1 = compA.getCompound(key);
                if (tmp1 == null) {
                    return;
                }
                if (typeMismatch) {
                    saveTo.addCompound(key).mergeCompound(tmp1);
                } else {
                    NBTCompound tmp2 = compB.getCompound(key);
                    if (tmp2 == null) {
                        saveTo.addCompound(key).mergeCompound(tmp1);
                        return;
                    }
                    NBTCompound tmpDiff = tmp1.extractDifference(tmp2);
                    if (!tmpDiff.getKeys().isEmpty()) {
                        saveTo.addCompound(key).mergeCompound(tmpDiff);
                    }
                }
                return;
            }
            case NBTTagDouble: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setDouble(key, compA.getDouble(key));
                }
                return;
            }
            case NBTTagEnd: {
                return;
            }
            case NBTTagFloat: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setFloat(key, compA.getFloat(key));
                }
                return;
            }
            case NBTTagInt: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setInteger(key, compA.getInteger(key));
                }
                return;
            }
            case NBTTagIntArray: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setIntArray(key, compA.getIntArray(key));
                }
                return;
            }
            case NBTTagList: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.set(key, NBTReflectionUtil.getEntry(compA, key));
                }
                return;
            }
            case NBTTagLong: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setLong(key, compA.getLong(key));
                }
                return;
            }
            case NBTTagShort: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setShort(key, compA.getShort(key));
                }
                return;
            }
            case NBTTagString: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setString(key, compA.getString(key));
                }
                return;
            }
            case NBTTagLongArray: {
                if (typeMismatch || !NBTCompound.isEqual(compA, compB, key)) {
                    saveTo.setLongArray(key, compA.getLongArray(key));
                }
                return;
            }
        }
    }

    private static boolean isEqual(NBTCompound compA, NBTCompound compB, String key) {
        if (compA.getType(key) != compB.getType(key)) {
            return false;
        }
        switch (compA.getType(key)) {
            case NBTTagByte: {
                return compA.getByte(key).equals(compB.getByte(key));
            }
            case NBTTagByteArray: {
                return Arrays.equals(compA.getByteArray(key), compB.getByteArray(key));
            }
            case NBTTagCompound: {
                NBTCompound tmp = compA.getCompound(key);
                return tmp != null && tmp.equals(compB.getCompound(key));
            }
            case NBTTagDouble: {
                return compA.getDouble(key).equals(compB.getDouble(key));
            }
            case NBTTagEnd: {
                return true;
            }
            case NBTTagFloat: {
                return compA.getFloat(key).equals(compB.getFloat(key));
            }
            case NBTTagInt: {
                return compA.getInteger(key).equals(compB.getInteger(key));
            }
            case NBTTagIntArray: {
                return Arrays.equals(compA.getIntArray(key), compB.getIntArray(key));
            }
            case NBTTagList: {
                return NBTReflectionUtil.getEntry(compA, key).toString().equals(NBTReflectionUtil.getEntry(compB, key).toString());
            }
            case NBTTagLong: {
                return compA.getLong(key).equals(compB.getLong(key));
            }
            case NBTTagShort: {
                return compA.getShort(key).equals(compB.getShort(key));
            }
            case NBTTagString: {
                return compA.getString(key).equals(compB.getString(key));
            }
            case NBTTagLongArray: {
                return Arrays.equals(compA.getLongArray(key), compB.getLongArray(key));
            }
        }
        return false;
    }
}
