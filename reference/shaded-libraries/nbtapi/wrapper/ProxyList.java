/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.wrapper;

import com.fernsehheft.enderchest.nbtapi.wrapper.NBTProxy;

public interface ProxyList<T extends NBTProxy>
extends Iterable<T> {
    public T addCompound();

    public int size();

    public boolean isEmpty();

    public T get(int var1);

    public void remove(int var1);
}

