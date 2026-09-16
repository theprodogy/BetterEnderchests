/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi;

public enum NBTType {
    NBTTagEnd(0, ""),
    NBTTagByte(1, "BYTE"),
    NBTTagShort(2, "SHORT"),
    NBTTagInt(3, "INT"),
    NBTTagLong(4, "LONG"),
    NBTTagFloat(5, "FLOAT"),
    NBTTagDouble(6, "DOUBLE"),
    NBTTagByteArray(7, "BYTE[]"),
    NBTTagString(8, "STRING"),
    NBTTagList(9, "LIST"),
    NBTTagCompound(10, "COMPOUND"),
    NBTTagIntArray(11, "INT[]"),
    NBTTagLongArray(12, "LONG[]");

    private final int id;
    private final String name;

    private NBTType(int i, String name) {
        this.id = i;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static NBTType valueOf(int id) {
        for (NBTType t : NBTType.values()) {
            if (t.getId() != id) continue;
            return t;
        }
        return NBTTagEnd;
    }

    public static NBTType fromName(String name) {
        for (NBTType t : NBTType.values()) {
            if (!t.getName().equals(name)) continue;
            return t;
        }
        return NBTTagEnd;
    }
}

