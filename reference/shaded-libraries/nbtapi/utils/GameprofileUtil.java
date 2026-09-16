/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 *  javax.annotation.Nullable
 */
package com.fernsehheft.enderchest.nbtapi.utils;

import com.fernsehheft.enderchest.nbtapi.NBTType;
import com.fernsehheft.enderchest.nbtapi.NbtApiException;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadWriteNBTCompoundList;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBT;
import com.fernsehheft.enderchest.nbtapi.iface.ReadableNBTList;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import javax.annotation.Nullable;

public class GameprofileUtil {
    private static Method GET_NAME = null;
    private static Method GET_ID = null;
    private static Method GET_VALUE = null;
    private static Method GET_SIGNATURE = null;
    private static Method GET_PROPERTY_NAME = null;
    private static Method GET_PROPERTIES = null;

    @Nullable
    public static GameProfile readGameProfile(ReadableNBT arg) {
        String string = null;
        UUID uUID = null;
        if (arg.hasTag("Name") && arg.getType("Name") == NBTType.NBTTagString) {
            string = arg.getString("Name");
        } else if (arg.hasTag("name") && arg.getType("name") == NBTType.NBTTagString) {
            string = arg.getString("name");
        }
        if (arg.hasTag("Id") && arg.getType("Id") == NBTType.NBTTagIntArray && arg.getIntArray("Id").length == 4) {
            uUID = arg.getUUID("Id");
        } else if (arg.hasTag("id") && arg.getType("id") == NBTType.NBTTagIntArray && arg.getIntArray("id").length == 4) {
            uUID = arg.getUUID("id");
        }
        try {
            GameProfile gameProfile = new GameProfile(uUID, string);
            if (arg.hasTag("Properties") && arg.getType("Properties") == NBTType.NBTTagCompound) {
                ReadableNBT compoundTag = arg.getCompound("Properties");
                for (String string2 : compoundTag.getKeys()) {
                    ReadableNBTList<ReadWriteNBT> listTag = compoundTag.getCompoundList(string);
                    for (int i = 0; i < listTag.size(); ++i) {
                        ReadableNBT compoundTag2 = listTag.get(i);
                        String string3 = compoundTag2.getString("Value");
                        if (compoundTag2.hasTag("Signature") && compoundTag2.getType("Signature") == NBTType.NBTTagString) {
                            gameProfile.getProperties().put((Object)string2, (Object)new Property(string2, string3, compoundTag2.getString("Signature")));
                            continue;
                        }
                        gameProfile.getProperties().put((Object)string2, (Object)new Property(string2, string3));
                    }
                }
            } else if (arg.getType("properties") == NBTType.NBTTagList) {
                ReadableNBTList<ReadWriteNBT> listTag = arg.getCompoundList("properties");
                for (int i = 0; i < listTag.size(); ++i) {
                    ReadableNBT compoundTag2 = listTag.get(i);
                    String string2 = compoundTag2.getString("name");
                    String string3 = compoundTag2.getString("value");
                    if (compoundTag2.hasTag("signature") && compoundTag2.getType("signature") == NBTType.NBTTagString) {
                        gameProfile.getProperties().put((Object)string2, (Object)new Property(string2, string3, compoundTag2.getString("signature")));
                        continue;
                    }
                    gameProfile.getProperties().put((Object)string2, (Object)new Property(string2, string3));
                }
            }
            return gameProfile;
        }
        catch (Throwable var11) {
            return null;
        }
    }

    public static ReadWriteNBT writeGameProfile(ReadWriteNBT arg, GameProfile gameProfile) {
        block7: {
            PropertyMap properties;
            UUID id;
            String name = GameprofileUtil.getName(gameProfile);
            if (name != null && !name.isEmpty()) {
                String nameKey = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4) ? "name" : "Name";
                arg.setString(nameKey, GameprofileUtil.getName(gameProfile));
            }
            if ((id = GameprofileUtil.getId(gameProfile)) != null) {
                String idKey = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4) ? "id" : "Id";
                arg.setUUID(idKey, id);
            }
            if ((properties = GameprofileUtil.getProperties(gameProfile)).isEmpty()) break block7;
            if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
                ReadWriteNBTCompoundList list = arg.getCompoundList("properties");
                for (Property property : properties.values()) {
                    ReadWriteNBT tag = list.addCompound();
                    tag.setString("name", GameprofileUtil.getPropertyName(property));
                    tag.setString("value", GameprofileUtil.getValue(property));
                    if (!property.hasSignature()) continue;
                    tag.setString("signature", GameprofileUtil.getSignature(property));
                }
            } else {
                ReadWriteNBT compoundTag = arg.getOrCreateCompound("Properties");
                for (String string : gameProfile.getProperties().keySet()) {
                    ReadWriteNBTCompoundList list = compoundTag.getCompoundList(string);
                    for (Property property : properties.get((Object)string)) {
                        ReadWriteNBT tag = list.addCompound();
                        tag.setString("Value", GameprofileUtil.getValue(property));
                        if (!property.hasSignature()) continue;
                        tag.setString("Signature", GameprofileUtil.getSignature(property));
                    }
                }
            }
        }
        return arg;
    }

    private static String getName(GameProfile profile) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R6)) {
            try {
                return GET_NAME.invoke((Object)profile, new Object[0]).toString();
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new NbtApiException("Failed to get GameProfile name via reflection", e);
            }
        }
        return profile.getName();
    }

    private static UUID getId(GameProfile profile) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R6)) {
            try {
                return (UUID)GET_ID.invoke((Object)profile, new Object[0]);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new NbtApiException("Failed to get GameProfile id via reflection", e);
            }
        }
        return profile.getId();
    }

    private static String getValue(Property property) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R6)) {
            try {
                return GET_VALUE.invoke((Object)property, new Object[0]).toString();
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new NbtApiException("Failed to get Property value via reflection", e);
            }
        }
        return property.getValue();
    }

    private static String getSignature(Property property) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R6)) {
            try {
                return GET_SIGNATURE.invoke((Object)property, new Object[0]).toString();
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new NbtApiException("Failed to get Property signature via reflection", e);
            }
        }
        return property.getSignature();
    }

    private static String getPropertyName(Property property) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R6)) {
            try {
                return GET_PROPERTY_NAME.invoke((Object)property, new Object[0]).toString();
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new NbtApiException("Failed to get Property name via reflection", e);
            }
        }
        return property.getName();
    }

    private static PropertyMap getProperties(GameProfile profile) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R6)) {
            try {
                return (PropertyMap)GET_PROPERTIES.invoke((Object)profile, new Object[0]);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new NbtApiException("Failed to get GameProfile properties via reflection", e);
            }
        }
        return profile.getProperties();
    }

    static {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R6)) {
            try {
                GET_NAME = GameProfile.class.getDeclaredMethod("name", new Class[0]);
                GET_ID = GameProfile.class.getDeclaredMethod("id", new Class[0]);
                GET_VALUE = Property.class.getDeclaredMethod("value", new Class[0]);
                GET_SIGNATURE = Property.class.getDeclaredMethod("signature", new Class[0]);
                GET_PROPERTY_NAME = Property.class.getDeclaredMethod("name", new Class[0]);
                GET_PROPERTIES = GameProfile.class.getDeclaredMethod("properties", new Class[0]);
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}

