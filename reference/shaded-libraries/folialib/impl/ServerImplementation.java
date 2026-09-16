/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 */
package com.fernsehheft.enderchest.folialib.impl;

import com.fernsehheft.enderchest.folialib.enums.EntityTaskResult;
import com.fernsehheft.enderchest.folialib.wrapper.task.WrappedTask;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public interface ServerImplementation {
    public boolean isOwnedByCurrentRegion(@NotNull Location var1);

    public boolean isOwnedByCurrentRegion(@NotNull Location var1, int var2);

    public boolean isOwnedByCurrentRegion(@NotNull Block var1);

    public boolean isOwnedByCurrentRegion(@NotNull World var1, int var2, int var3);

    public boolean isOwnedByCurrentRegion(@NotNull World var1, int var2, int var3, int var4);

    public boolean isOwnedByCurrentRegion(@NotNull Entity var1);

    public boolean isGlobalTickThread();

    @NotNull
    public CompletableFuture<Void> runNextTick(@NotNull Consumer<WrappedTask> var1);

    @NotNull
    public CompletableFuture<Void> runAsync(@NotNull Consumer<WrappedTask> var1);

    public WrappedTask runLater(@NotNull Runnable var1, long var2);

    @NotNull
    public CompletableFuture<Void> runLater(@NotNull Consumer<WrappedTask> var1, long var2);

    public WrappedTask runLater(@NotNull Runnable var1, long var2, TimeUnit var4);

    @NotNull
    public CompletableFuture<Void> runLater(@NotNull Consumer<WrappedTask> var1, long var2, TimeUnit var4);

    public WrappedTask runLaterAsync(@NotNull Runnable var1, long var2);

    @NotNull
    public CompletableFuture<Void> runLaterAsync(@NotNull Consumer<WrappedTask> var1, long var2);

    public WrappedTask runLaterAsync(@NotNull Runnable var1, long var2, TimeUnit var4);

    @NotNull
    public CompletableFuture<Void> runLaterAsync(@NotNull Consumer<WrappedTask> var1, long var2, TimeUnit var4);

    public WrappedTask runTimer(@NotNull Runnable var1, long var2, long var4);

    public void runTimer(@NotNull Consumer<WrappedTask> var1, long var2, long var4);

    public WrappedTask runTimer(@NotNull Runnable var1, long var2, long var4, TimeUnit var6);

    public void runTimer(@NotNull Consumer<WrappedTask> var1, long var2, long var4, TimeUnit var6);

    public WrappedTask runTimerAsync(@NotNull Runnable var1, long var2, long var4);

    public void runTimerAsync(@NotNull Consumer<WrappedTask> var1, long var2, long var4);

    public WrappedTask runTimerAsync(@NotNull Runnable var1, long var2, long var4, TimeUnit var6);

    public void runTimerAsync(@NotNull Consumer<WrappedTask> var1, long var2, long var4, TimeUnit var6);

    @NotNull
    public CompletableFuture<Void> runAtLocation(Location var1, @NotNull Consumer<WrappedTask> var2);

    public WrappedTask runAtLocationLater(Location var1, @NotNull Runnable var2, long var3);

    @NotNull
    public CompletableFuture<Void> runAtLocationLater(Location var1, @NotNull Consumer<WrappedTask> var2, long var3);

    public WrappedTask runAtLocationLater(Location var1, @NotNull Runnable var2, long var3, TimeUnit var5);

    @NotNull
    public CompletableFuture<Void> runAtLocationLater(Location var1, @NotNull Consumer<WrappedTask> var2, long var3, TimeUnit var5);

    public WrappedTask runAtLocationTimer(Location var1, @NotNull Runnable var2, long var3, long var5);

    public void runAtLocationTimer(Location var1, @NotNull Consumer<WrappedTask> var2, long var3, long var5);

    public WrappedTask runAtLocationTimer(Location var1, @NotNull Runnable var2, long var3, long var5, TimeUnit var7);

    public void runAtLocationTimer(Location var1, @NotNull Consumer<WrappedTask> var2, long var3, long var5, TimeUnit var7);

    @NotNull
    public CompletableFuture<EntityTaskResult> runAtEntity(Entity var1, @NotNull Consumer<WrappedTask> var2);

    @NotNull
    public CompletableFuture<EntityTaskResult> runAtEntityWithFallback(Entity var1, @NotNull Consumer<WrappedTask> var2, @Nullable Runnable var3);

    public WrappedTask runAtEntityLater(Entity var1, @NotNull Runnable var2, long var3);

    public WrappedTask runAtEntityLater(Entity var1, @NotNull Runnable var2, @Nullable Runnable var3, long var4);

    @NotNull
    public CompletableFuture<Void> runAtEntityLater(Entity var1, @NotNull Consumer<WrappedTask> var2, long var3);

    @NotNull
    public CompletableFuture<Void> runAtEntityLater(Entity var1, @NotNull Consumer<WrappedTask> var2, Runnable var3, long var4);

    public WrappedTask runAtEntityLater(Entity var1, @NotNull Runnable var2, long var3, TimeUnit var5);

    @NotNull
    public CompletableFuture<Void> runAtEntityLater(Entity var1, @NotNull Consumer<WrappedTask> var2, long var3, TimeUnit var5);

    public WrappedTask runAtEntityTimer(Entity var1, @NotNull Runnable var2, long var3, long var5);

    public WrappedTask runAtEntityTimer(Entity var1, @NotNull Runnable var2, Runnable var3, long var4, long var6);

    public void runAtEntityTimer(Entity var1, @NotNull Consumer<WrappedTask> var2, long var3, long var5);

    public void runAtEntityTimer(Entity var1, @NotNull Consumer<WrappedTask> var2, Runnable var3, long var4, long var6);

    public WrappedTask runAtEntityTimer(Entity var1, @NotNull Runnable var2, long var3, long var5, TimeUnit var7);

    public void runAtEntityTimer(Entity var1, @NotNull Consumer<WrappedTask> var2, long var3, long var5, TimeUnit var7);

    public void cancelTask(WrappedTask var1);

    public void cancelAllTasks();

    public List<WrappedTask> getAllTasks();

    public List<WrappedTask> getAllServerTasks();

    public Player getPlayer(String var1);

    public Player getPlayerExact(String var1);

    public Player getPlayer(UUID var1);

    public CompletableFuture<Boolean> teleportAsync(Entity var1, Location var2);

    public CompletableFuture<Boolean> teleportAsync(Entity var1, Location var2, PlayerTeleportEvent.TeleportCause var3);

    public WrappedTask wrapTask(Object var1);
}

