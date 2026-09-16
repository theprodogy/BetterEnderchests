/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBT;
import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.NBTContainer;
import com.fernsehheft.enderchest.nbtapi.NBTReflectionUtil;
import com.fernsehheft.enderchest.nbtapi.utils.GameprofileUtil;
import com.fernsehheft.enderchest.nbtapi.utils.MinecraftVersion;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ObjectCreator;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import com.mojang.authlib.GameProfile;

public class NBTGameProfile {
    @Deprecated
    public static NBTCompound toNBT(GameProfile profile) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            return (NBTCompound)GameprofileUtil.writeGameProfile(NBT.createNBTObject(), profile);
        }
        return new NBTContainer(ReflectionMethod.GAMEPROFILE_SERIALIZE.run(null, ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance(new Object[0]), profile));
    }

    @Deprecated
    public static GameProfile fromNBT(NBTCompound compound) {
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            return GameprofileUtil.readGameProfile(compound);
        }
        return (GameProfile)ReflectionMethod.GAMEPROFILE_DESERIALIZE.run(null, NBTReflectionUtil.getToCompount(compound.getCompound(), compound));
    }
}

