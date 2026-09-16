/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTReflectionUtil;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ClassWrapper;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ObjectCreator;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.io.InputStream;

public class NBTContainer
extends NBTCompound {
    private Object nbt;
    private boolean closed;
    private boolean readOnly;

    @Deprecated
    public NBTContainer() {
        super(null, null);
        this.nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
    }

    @Deprecated
    public NBTContainer(Object nbt) {
        super(null, null);
        if (nbt == null) {
            nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]);
        }
        if (!ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().isAssignableFrom(nbt.getClass())) {
            throw new NbtApiException("The object '" + nbt.getClass() + "' is not a valid NBT-Object!");
        }
        this.nbt = nbt;
    }

    @Deprecated
    public NBTContainer(InputStream inputsteam) {
        super(null, null);
        this.nbt = NBTReflectionUtil.readNBT(inputsteam);
    }

    @Deprecated
    public NBTContainer(String nbtString) {
        super(null, null);
        if (nbtString == null) {
            throw new NullPointerException("The String can't be null!");
        }
        try {
            this.nbt = ReflectionMethod.PARSE_NBT.run(null, nbtString);
        }
        catch (Exception ex) {
            throw new NbtApiException("Unable to parse Malformed Json!", ex);
        }
    }

    @Override
    public Object getCompound() {
        return this.nbt;
    }

    @Override
    public void setCompound(Object tag) {
        this.nbt = tag;
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
    protected boolean isReadOnly() {
        return this.readOnly;
    }

    protected NBTContainer setReadOnly(boolean readOnly) {
        this.readOnly = true;
        return this;
    }
}

