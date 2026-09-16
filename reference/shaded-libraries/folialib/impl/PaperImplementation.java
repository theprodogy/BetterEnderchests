/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Entity
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 */
package com.fernsehheft.enderchest.folialib.impl;

import com.fernsehheft.enderchest.folialib.FoliaLib;
import com.fernsehheft.enderchest.folialib.impl.SpigotImplementation;
import com.fernsehheft.enderchest.folialib.util.ImplementationTestsUtil;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PaperImplementation
extends SpigotImplementation {
    private Method teleportAsyncMethod;

    public PaperImplementation(FoliaLib foliaLib) {
        super(foliaLib);
        if (ImplementationTestsUtil.isAsyncTeleportSupported()) {
            try {
                this.teleportAsyncMethod = Entity.class.getMethod("teleportAsync", Location.class, PlayerTeleportEvent.TeleportCause.class);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException("Failed to initialize PaperImplementation", e);
            }
        }
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return this.teleportAsync(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        if (!ImplementationTestsUtil.isAsyncTeleportSupported()) {
            return super.teleportAsync(entity, location, cause);
        }
        try {
            return (CompletableFuture)this.teleportAsyncMethod.invoke((Object)entity, location, cause);
        }
        catch (Exception e) {
            e.printStackTrace();
            return super.teleportAsync(entity, location, cause);
        }
    }
}

