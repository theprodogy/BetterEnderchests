/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.iface;

import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import java.io.File;
import java.io.IOException;

public interface NBTFileHandle
extends ReadWriteNBT {
    public void save() throws IOException;

    public File getFile();
}

