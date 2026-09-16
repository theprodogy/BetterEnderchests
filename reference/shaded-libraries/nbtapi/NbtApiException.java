/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;

public class NbtApiException
extends RuntimeException {
    private static final long serialVersionUID = -993309714559452334L;
    public static Boolean confirmedBroken = null;

    public NbtApiException() {
    }

    public NbtApiException(String message, Throwable cause) {
        super(NbtApiException.generateMessage(message), cause);
    }

    public NbtApiException(String message) {
        super(NbtApiException.generateMessage(message));
    }

    public NbtApiException(Throwable cause) {
        super(NbtApiException.generateMessage(cause == null ? null : cause.toString()), cause);
    }

    private static String generateMessage(String message) {
        if (message == null) {
            return null;
        }
        if (confirmedBroken == null) {
            return "[?][" + MinecraftVersion.getNBTAPIVersion() + "]" + message;
        }
        if (!confirmedBroken.booleanValue()) {
            return "[Selfchecked][" + MinecraftVersion.getNBTAPIVersion() + "]" + message;
        }
        return "[" + (Object)((Object)MinecraftVersion.getVersion()) + "][" + MinecraftVersion.getNBTAPIVersion() + "]There were errors detected during the server self-check! Please, make sure that NBT-API is up to date. Error message: " + message;
    }
}

