/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.persistence.PersistentDataContainer
 */
package com.fernsehheft.enderchest.nbtapi;

import com.fernsehheft.enderchest.nbtapi.NBTCompound;
import com.fernsehheft.enderchest.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.util.Map;
import org.bukkit.persistence.PersistentDataContainer;

public class NBTPersistentDataContainer
extends NBTCompound {
    private final PersistentDataContainer container;

    public NBTPersistentDataContainer(PersistentDataContainer container) {
        super(null, null);
        this.container = container;
    }

    @Override
    public Object getCompound() {
        return ReflectionMethod.CRAFT_PERSISTENT_DATA_CONTAINER_TO_TAG.run(this.container, new Object[0]);
    }

    @Override
    protected void setCompound(Object compound) {
        Map map = (Map)ReflectionMethod.CRAFT_PERSISTENT_DATA_CONTAINER_GET_MAP.run(this.container, new Object[0]);
        map.clear();
        ReflectionMethod.CRAFT_PERSISTENT_DATA_CONTAINER_PUT_ALL.run(this.container, compound);
    }
}

