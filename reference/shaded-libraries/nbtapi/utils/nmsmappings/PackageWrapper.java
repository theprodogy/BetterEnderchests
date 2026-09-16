/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.utils.nmsmappings;

public enum PackageWrapper {
    NMS(new String(new byte[]{110, 101, 116, 46, 109, 105, 110, 101, 99, 114, 97, 102, 116, 46, 115, 101, 114, 118, 101, 114})),
    CRAFTBUKKIT(new String(new byte[]{111, 114, 103, 46, 98, 117, 107, 107, 105, 116, 46, 99, 114, 97, 102, 116, 98, 117, 107, 107, 105, 116})),
    NONE("");

    private final String uri;

    private PackageWrapper(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return this.uri;
    }
}

