/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTContainer;
import com.fernsehheft.enderchest.nbtapi.NBTReflectionUtil;
import com.fernsehheft.enderchest.nbtapi.iface.NBTFileHandle;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ObjectCreator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;

public class NBTFile
extends NBTCompound
implements NBTFileHandle {
    private final File file;
    private Object nbt;

    @Deprecated
    public NBTFile(File file) throws IOException {
        super(null, null);
        if (file == null) {
            throw new NullPointerException("File can't be null!");
        }
        this.file = file;
        if (file.exists()) {
            this.nbt = NBTReflectionUtil.readNBT(Files.newInputStream(file.toPath(), new OpenOption[0]));
        } else {
            this.nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
            this.save();
        }
    }

    @Override
    public void save() throws IOException {
        try {
            this.getWriteLock().lock();
            NBTFile.saveTo(this.file, this);
        }
        finally {
            this.getWriteLock().unlock();
        }
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public Object getCompound() {
        return this.nbt;
    }

    @Override
    protected void setCompound(Object compound) {
        this.nbt = compound;
    }

    @Deprecated
    public static NBTCompound readFrom(File file) throws IOException {
        if (!file.exists()) {
            return new NBTContainer();
        }
        return new NBTContainer(NBTReflectionUtil.readNBT(Files.newInputStream(file.toPath(), new OpenOption[0])));
    }

    @Deprecated
    public static void saveTo(File file, NBTCompound nbt) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                throw new IOException("Unable to create file at " + file.getAbsolutePath());
            }
        }
        nbt.writeCompound(Files.newOutputStream(file.toPath(), new OpenOption[0]));
    }
}

