/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.iface;

import com.fernsehheft.enderchest.nbtapi.NBTType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ReadableNBTList<T>
extends Iterable<T> {
    public T get(int var1);

    public int size();

    public NBTType getType();

    public boolean isEmpty();

    public boolean contains(Object var1);

    public int indexOf(Object var1);

    public boolean containsAll(Collection<?> var1);

    public int lastIndexOf(Object var1);

    public Object[] toArray();

    public <E> E[] toArray(E[] var1);

    public List<T> subList(int var1, int var2);

    default public List<T> toListCopy() {
        ArrayList list = new ArrayList();
        this.iterator().forEachRemaining(list::add);
        return list;
    }
}

