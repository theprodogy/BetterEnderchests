/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.iface;

import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBTList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.function.Predicate;

public interface ReadWriteNBTList<T>
extends ReadableNBTList<T> {
    public boolean add(T var1);

    public void add(int var1, T var2);

    public T set(int var1, T var2);

    public T remove(int var1);

    public void clear();

    public boolean addAll(Collection<? extends T> var1);

    public boolean addAll(int var1, Collection<? extends T> var2);

    public boolean removeAll(Collection<?> var1);

    public boolean retainAll(Collection<?> var1);

    public boolean removeIf(Predicate<? super T> var1);

    public boolean remove(Object var1);

    public ListIterator<T> listIterator(int var1);
}

