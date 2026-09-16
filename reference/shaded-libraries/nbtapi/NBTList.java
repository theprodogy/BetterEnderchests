/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTType;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBTList;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public abstract class NBTList<T>
implements List<T>,
ReadWriteNBTList<T> {
    private String listName;
    private NBTCompound parent;
    private NBTType type;
    protected Object listObject;

    protected NBTList(NBTCompound owner, String name, NBTType type, Object list) {
        this.parent = owner;
        this.listName = name;
        this.type = type;
        this.listObject = list;
    }

    public String getName() {
        return this.listName;
    }

    public NBTCompound getParent() {
        return this.parent;
    }

    private void validateClosed() {
        if (this.parent.isClosed()) {
            throw new NbtApiException("Tried using closed NBT data!");
        }
    }

    private void validateWritable() {
        if (this.getParent().isReadOnly()) {
            throw new NbtApiException("Tried setting data in read only mode!");
        }
    }

    protected void save() {
        this.validateClosed();
        this.parent.set(this.listName, this.listObject);
    }

    protected abstract Object asTag(T var1);

    @Override
    public boolean add(T element) {
        this.validateClosed();
        this.validateWritable();
        try {
            this.parent.getWriteLock().lock();
            if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
                ReflectionMethod.LIST_ADD.run(this.listObject, this.size(), this.asTag(element));
            } else {
                ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, this.asTag(element));
            }
            this.save();
            boolean bl = true;
            return bl;
        }
        catch (Exception ex) {
            throw new NbtApiException(ex);
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    @Override
    public void add(int index, T element) {
        this.validateClosed();
        this.validateWritable();
        try {
            this.parent.getWriteLock().lock();
            if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
                ReflectionMethod.LIST_ADD.run(this.listObject, index, this.asTag(element));
            } else {
                ReflectionMethod.LEGACY_LIST_ADD.run(this.listObject, this.asTag(element));
            }
            this.save();
        }
        catch (Exception ex) {
            throw new NbtApiException(ex);
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    @Override
    public T set(int index, T element) {
        this.validateClosed();
        this.validateWritable();
        try {
            this.parent.getWriteLock().lock();
            Object prev = this.get(index);
            ReflectionMethod.LIST_SET.run(this.listObject, index, this.asTag(element));
            this.save();
            Object e = prev;
            return (T)e;
        }
        catch (Exception ex) {
            throw new NbtApiException(ex);
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    @Override
    public T remove(int i) {
        this.validateClosed();
        this.validateWritable();
        try {
            this.parent.getWriteLock().lock();
            Object old = this.get(i);
            ReflectionMethod.LIST_REMOVE_KEY.run(this.listObject, i);
            this.save();
            Object e = old;
            return (T)e;
        }
        catch (Exception ex) {
            throw new NbtApiException(ex);
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    @Override
    public int size() {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            int n = (Integer)ReflectionMethod.LIST_SIZE.run(this.listObject, new Object[0]);
            return n;
        }
        catch (Exception ex) {
            throw new NbtApiException(ex);
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }

    @Override
    public NBTType getType() {
        return this.type;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public void clear() {
        while (!this.isEmpty()) {
            this.remove(0);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean contains(Object o) {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            for (int i = 0; i < this.size(); ++i) {
                if (!o.equals(this.get(i))) continue;
                boolean bl = true;
                return bl;
            }
            boolean bl = false;
            return bl;
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int indexOf(Object o) {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            for (int i = 0; i < this.size(); ++i) {
                if (!o.equals(this.get(i))) continue;
                int n = i;
                return n;
            }
            int n = -1;
            return n;
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        this.validateClosed();
        try {
            this.parent.getWriteLock().lock();
            int size = this.size();
            for (T ele : c) {
                this.add(ele);
            }
            boolean bl = size != this.size();
            return bl;
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        this.validateClosed();
        try {
            this.parent.getWriteLock().lock();
            int size = this.size();
            for (T ele : c) {
                this.add(index++, ele);
            }
            boolean bl = size != this.size();
            return bl;
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            for (Object ele : c) {
                if (this.contains(ele)) continue;
                boolean bl = false;
                return bl;
            }
            boolean bl = true;
            return bl;
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int lastIndexOf(Object o) {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            int index = -1;
            for (int i = 0; i < this.size(); ++i) {
                if (!o.equals(this.get(i))) continue;
                index = i;
            }
            int n = index;
            return n;
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        this.validateClosed();
        try {
            this.parent.getWriteLock().lock();
            int size = this.size();
            for (Object obj : c) {
                this.remove(obj);
            }
            boolean bl = size != this.size();
            return bl;
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        this.validateClosed();
        try {
            this.parent.getWriteLock().lock();
            int size = this.size();
            for (Object obj : c) {
                for (int i = 0; i < this.size(); ++i) {
                    if (obj.equals(this.get(i))) continue;
                    this.remove(i--);
                }
            }
            boolean bl = size != this.size();
            return bl;
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean remove(Object o) {
        this.validateClosed();
        try {
            this.parent.getWriteLock().lock();
            int size = this.size();
            int id = this.indexOf(o);
            if (id != -1) {
                this.remove(id);
            }
            boolean bl = size != this.size();
            return bl;
        }
        finally {
            this.parent.getWriteLock().unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>(){
            private int index = -1;

            @Override
            public boolean hasNext() {
                return NBTList.this.size() > this.index + 1;
            }

            @Override
            public T next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return NBTList.this.get(++this.index);
            }

            @Override
            public void remove() {
                NBTList.this.remove(this.index);
                --this.index;
            }
        };
    }

    @Override
    public ListIterator<T> listIterator() {
        return this.listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(final int startIndex) {
        final NBTList list = this;
        return new ListIterator<T>(){
            int index;
            {
                this.index = startIndex - 1;
            }

            @Override
            public void add(T e) {
                list.add(this.index, e);
            }

            @Override
            public boolean hasNext() {
                return NBTList.this.size() > this.index + 1;
            }

            @Override
            public boolean hasPrevious() {
                return this.index >= 0 && this.index <= NBTList.this.size();
            }

            @Override
            public T next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return NBTList.this.get(++this.index);
            }

            @Override
            public int nextIndex() {
                return this.index + 1;
            }

            @Override
            public T previous() {
                if (!this.hasPrevious()) {
                    throw new NoSuchElementException("Id: " + (this.index - 1));
                }
                return NBTList.this.get(this.index--);
            }

            @Override
            public int previousIndex() {
                return this.index - 1;
            }

            @Override
            public void remove() {
                list.remove(this.index);
                --this.index;
            }

            @Override
            public void set(T e) {
                list.set(this.index, e);
            }
        };
    }

    @Override
    public Object[] toArray() {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            Object[] ar = new Object[this.size()];
            for (int i = 0; i < this.size(); ++i) {
                ar[i] = this.get(i);
            }
            Object[] objectArray = ar;
            return objectArray;
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public <E> E[] toArray(E[] a) {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            Object[] ar = Arrays.copyOf(a, this.size());
            Arrays.fill(ar, null);
            Class<?> arrayclass = a.getClass().getComponentType();
            for (int i = 0; i < this.size(); ++i) {
                Object obj = this.get(i);
                if (!arrayclass.isInstance(obj)) {
                    throw new ArrayStoreException("The array does not match the objects stored in the List.");
                }
                ar[i] = this.get(i);
            }
            Object[] objectArray = ar;
            return objectArray;
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            ArrayList list = new ArrayList();
            for (int i = fromIndex; i < toIndex; ++i) {
                list.add(this.get(i));
            }
            ArrayList arrayList = list;
            return arrayList;
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return List.super.removeIf(filter);
    }

    public String toString() {
        this.validateClosed();
        try {
            this.parent.getReadLock().lock();
            String string = this.listObject.toString();
            return string;
        }
        finally {
            this.parent.getReadLock().unlock();
        }
    }
}

