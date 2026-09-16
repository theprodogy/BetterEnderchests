/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package com.fernsehheft.enderchest.faststats.core;

import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.Contract;

final class MurmurHash3 {
    MurmurHash3() {
    }

    public static String hash(JsonObject object) {
        long[] hash = MurmurHash3.hash(object.toString());
        return Long.toHexString(hash[0]) + Long.toHexString(hash[1]);
    }

    @Contract(value="_ -> new", pure=true)
    private static long[] hash(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        long h1 = 0L;
        long h2 = 0L;
        long c1 = -8663945395140668459L;
        long c2 = 5545529020109919103L;
        int length = bytes.length;
        int blocks = length / 16;
        for (int i = 0; i < blocks; ++i) {
            int k1 = MurmurHash3.getInt(bytes, i * 16);
            int k2 = MurmurHash3.getInt(bytes, i * 16 + 4);
            int k3 = MurmurHash3.getInt(bytes, i * 16 + 8);
            int k4 = MurmurHash3.getInt(bytes, i * 16 + 12);
            k1 *= 289559509;
            k1 = Integer.rotateLeft(k1, 31);
            h1 ^= (long)(k1 *= 658871167);
            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5L + 1390208809L;
            k2 *= 658871167;
            k2 = Integer.rotateLeft(k2, 33);
            h2 ^= (long)(k2 *= 289559509);
            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5L + 944331445L;
        }
        int k1 = 0;
        int k2 = 0;
        int k3 = 0;
        int k4 = 0;
        int tail = blocks * 16;
        switch (length & 0xF) {
            case 15: {
                k4 ^= (bytes[tail + 14] & 0xFF) << 16;
            }
            case 14: {
                k4 ^= (bytes[tail + 13] & 0xFF) << 8;
            }
            case 13: {
                k4 ^= bytes[tail + 12] & 0xFF;
                k4 *= 658871167;
                k4 = Integer.rotateLeft(k4, 33);
                h2 ^= (long)(k4 *= 289559509);
            }
            case 12: {
                k3 ^= (bytes[tail + 11] & 0xFF) << 24;
            }
            case 11: {
                k3 ^= (bytes[tail + 10] & 0xFF) << 16;
            }
            case 10: {
                k3 ^= (bytes[tail + 9] & 0xFF) << 8;
            }
            case 9: {
                k3 ^= bytes[tail + 8] & 0xFF;
                k3 *= 289559509;
                k3 = Integer.rotateLeft(k3, 31);
                h1 ^= (long)(k3 *= 658871167);
            }
            case 8: {
                k2 ^= (bytes[tail + 7] & 0xFF) << 24;
            }
            case 7: {
                k2 ^= (bytes[tail + 6] & 0xFF) << 16;
            }
            case 6: {
                k2 ^= (bytes[tail + 5] & 0xFF) << 8;
            }
            case 5: {
                k2 ^= bytes[tail + 4] & 0xFF;
                k2 *= 658871167;
                k2 = Integer.rotateLeft(k2, 33);
                h2 ^= (long)(k2 *= 289559509);
            }
            case 4: {
                k1 ^= (bytes[tail + 3] & 0xFF) << 24;
            }
            case 3: {
                k1 ^= (bytes[tail + 2] & 0xFF) << 16;
            }
            case 2: {
                k1 ^= (bytes[tail + 1] & 0xFF) << 8;
            }
            case 1: {
                k1 ^= bytes[tail] & 0xFF;
                k1 *= 289559509;
                k1 = Integer.rotateLeft(k1, 31);
                h1 ^= (long)(k1 *= 658871167);
            }
        }
        h1 ^= (long)length;
        h1 += (h2 ^= (long)length);
        h2 += h1;
        h1 = MurmurHash3.fmix64(h1);
        h2 = MurmurHash3.fmix64(h2);
        h1 += h2;
        return new long[]{h1, h2 += h1};
    }

    @Contract(pure=true)
    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= -49064778989728563L;
        k ^= k >>> 33;
        k *= -4265267296055464877L;
        k ^= k >>> 33;
        return k;
    }

    @Contract(pure=true)
    private static int getInt(byte[] bytes, int offset) {
        return bytes[offset] & 0xFF | (bytes[offset + 1] & 0xFF) << 8 | (bytes[offset + 2] & 0xFF) << 16 | (bytes[offset + 3] & 0xFF) << 24;
    }
}

