/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.scheduler.BukkitScheduler
 *  org.bukkit.scheduler.BukkitTask
 */
package com.fernsehheft.enderchest.folialib.impl;

import com.fernsehheft.enderchest.folialib.FoliaLib;
import com.fernsehheft.enderchest.folialib.enums.EntityTaskResult;
import com.fernsehheft.enderchest.folialib.impl.PlatformScheduler;
import com.fernsehheft.enderchest.folialib.util.ImplementationTestsUtil;
import com.fernsehheft.enderchest.folialib.util.TimeConverter;
import com.fernsehheft.enderchest.folialib.wrapper.task.WrappedBukkitTask;
import com.fernsehheft.enderchest.folialib.wrapper.task.WrappedLegacyBukkitTask;
import com.fernsehheft.enderchest.folialib.wrapper.task.WrappedTask;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegacySpigotImplementation
implements PlatformScheduler {
    private final JavaPlugin plugin;
    @NotNull
    private final BukkitScheduler scheduler;

    public LegacySpigotImplementation(FoliaLib foliaLib) {
        this.plugin = foliaLib.getPlugin();
        this.scheduler = this.plugin.getServer().getScheduler();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location) {
        return this.plugin.getServer().isPrimaryThread();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Location location, int squareRadiusChunks) {
        return this.plugin.getServer().isPrimaryThread();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Block block) {
        return this.plugin.getServer().isPrimaryThread();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ) {
        return this.plugin.getServer().isPrimaryThread();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ, int squareRadiusChunks) {
        return this.plugin.getServer().isPrimaryThread();
    }

    @Override
    public boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
        return this.plugin.getServer().isPrimaryThread();
    }

    @Override
    public boolean isGlobalTickThread() {
        return this.plugin.getServer().isPrimaryThread();
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runNextTick(@NotNull Consumer<WrappedTask> consumer) {
        WrappedTask[] taskReference;
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTask((Plugin)this.plugin, () -> {
            consumer.accept(taskReference[0]);
            future.complete(null);
        }))};
        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runAsync(@NotNull Consumer<WrappedTask> consumer) {
        WrappedTask[] taskReference;
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskAsynchronously((Plugin)this.plugin, () -> {
            consumer.accept(taskReference[0]);
            future.complete(null);
        }))};
        return future;
    }

    @Override
    public WrappedTask runLater(@NotNull Runnable runnable, long delay) {
        return this.wrapTask(this.scheduler.runTaskLater((Plugin)this.plugin, runnable, delay));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runLater(@NotNull Consumer<WrappedTask> consumer, long delay) {
        WrappedTask[] taskReference;
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskLater((Plugin)this.plugin, () -> {
            consumer.accept(taskReference[0]);
            future.complete(null);
        }, delay))};
        return future;
    }

    @Override
    public WrappedTask runLater(@NotNull Runnable runnable, long delay, TimeUnit unit) {
        return this.runLater(runnable, TimeConverter.toTicks(delay, unit));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runLater(@NotNull Consumer<WrappedTask> consumer, long delay, TimeUnit unit) {
        return this.runLater(consumer, TimeConverter.toTicks(delay, unit));
    }

    @Override
    public WrappedTask runLaterAsync(@NotNull Runnable runnable, long delay) {
        return this.wrapTask(this.scheduler.runTaskLaterAsynchronously((Plugin)this.plugin, runnable, delay));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runLaterAsync(@NotNull Consumer<WrappedTask> consumer, long delay) {
        WrappedTask[] taskReference;
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskLaterAsynchronously((Plugin)this.plugin, () -> {
            consumer.accept(taskReference[0]);
            future.complete(null);
        }, delay))};
        return future;
    }

    @Override
    public WrappedTask runLaterAsync(@NotNull Runnable runnable, long delay, TimeUnit unit) {
        return this.runLaterAsync(runnable, TimeConverter.toTicks(delay, unit));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runLaterAsync(@NotNull Consumer<WrappedTask> consumer, long delay, TimeUnit unit) {
        return this.runLaterAsync(consumer, TimeConverter.toTicks(delay, unit));
    }

    @Override
    public WrappedTask runTimer(@NotNull Runnable runnable, long delay, long period) {
        return this.wrapTask(this.scheduler.runTaskTimer((Plugin)this.plugin, runnable, delay, period));
    }

    @Override
    public void runTimer(@NotNull Consumer<WrappedTask> consumer, long delay, long period) {
        WrappedTask[] taskReference;
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskTimer((Plugin)this.plugin, () -> consumer.accept(taskReference[0]), delay, period))};
    }

    @Override
    public WrappedTask runTimer(@NotNull Runnable runnable, long delay, long period, TimeUnit unit) {
        return this.runTimer(runnable, TimeConverter.toTicks(delay, unit), TimeConverter.toTicks(period, unit));
    }

    @Override
    public void runTimer(@NotNull Consumer<WrappedTask> consumer, long delay, long period, TimeUnit unit) {
        this.runTimer(consumer, TimeConverter.toTicks(delay, unit), TimeConverter.toTicks(period, unit));
    }

    @Override
    public WrappedTask runTimerAsync(@NotNull Runnable runnable, long delay, long period) {
        return this.wrapTask(this.scheduler.runTaskTimerAsynchronously((Plugin)this.plugin, runnable, delay, period));
    }

    @Override
    public void runTimerAsync(@NotNull Consumer<WrappedTask> consumer, long delay, long period) {
        WrappedTask[] taskReference;
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskTimerAsynchronously((Plugin)this.plugin, () -> consumer.accept(taskReference[0]), delay, period))};
    }

    @Override
    public WrappedTask runTimerAsync(@NotNull Runnable runnable, long delay, long period, TimeUnit unit) {
        return this.runTimerAsync(runnable, TimeConverter.toTicks(delay, unit), TimeConverter.toTicks(period, unit));
    }

    @Override
    public void runTimerAsync(@NotNull Consumer<WrappedTask> consumer, long delay, long period, TimeUnit unit) {
        this.runTimerAsync(consumer, TimeConverter.toTicks(delay, unit), TimeConverter.toTicks(period, unit));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runAtLocation(Location location, @NotNull Consumer<WrappedTask> consumer) {
        return this.runNextTick(consumer);
    }

    @Override
    public WrappedTask runAtLocationLater(Location location, @NotNull Runnable runnable, long delay) {
        return this.wrapTask(this.scheduler.runTaskLater((Plugin)this.plugin, runnable, delay));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runAtLocationLater(Location location, @NotNull Consumer<WrappedTask> consumer, long delay) {
        WrappedTask[] taskReference;
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskLater((Plugin)this.plugin, () -> {
            consumer.accept(taskReference[0]);
            future.complete(null);
        }, delay))};
        return future;
    }

    @Override
    public WrappedTask runAtLocationLater(Location location, @NotNull Runnable runnable, long delay, TimeUnit unit) {
        return this.runAtLocationLater(location, runnable, TimeConverter.toTicks(delay, unit));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runAtLocationLater(Location location, @NotNull Consumer<WrappedTask> consumer, long delay, TimeUnit unit) {
        return this.runAtLocationLater(location, consumer, TimeConverter.toTicks(delay, unit));
    }

    @Override
    public WrappedTask runAtLocationTimer(Location location, @NotNull Runnable runnable, long delay, long period) {
        return this.wrapTask(this.scheduler.runTaskTimer((Plugin)this.plugin, runnable, delay, period));
    }

    @Override
    public void runAtLocationTimer(Location location, @NotNull Consumer<WrappedTask> consumer, long delay, long period) {
        WrappedTask[] taskReference;
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskTimer((Plugin)this.plugin, () -> consumer.accept(taskReference[0]), delay, period))};
    }

    @Override
    public WrappedTask runAtLocationTimer(Location location, @NotNull Runnable runnable, long delay, long period, TimeUnit unit) {
        return this.runAtLocationTimer(location, runnable, TimeConverter.toTicks(delay, unit), TimeConverter.toTicks(period, unit));
    }

    @Override
    public void runAtLocationTimer(Location location, @NotNull Consumer<WrappedTask> consumer, long delay, long period, TimeUnit unit) {
        this.runAtLocationTimer(location, consumer, TimeConverter.toTicks(delay, unit), TimeConverter.toTicks(period, unit));
    }

    @Override
    @NotNull
    public CompletableFuture<EntityTaskResult> runAtEntity(Entity entity, @NotNull Consumer<WrappedTask> consumer) {
        WrappedTask[] taskReference;
        CompletableFuture<EntityTaskResult> future = new CompletableFuture<EntityTaskResult>();
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTask((Plugin)this.plugin, () -> {
            consumer.accept(taskReference[0]);
            future.complete(EntityTaskResult.SUCCESS);
        }))};
        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<EntityTaskResult> runAtEntityWithFallback(Entity entity, @NotNull Consumer<WrappedTask> consumer, Runnable fallback) {
        WrappedTask[] taskReference;
        CompletableFuture<EntityTaskResult> future = new CompletableFuture<EntityTaskResult>();
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTask((Plugin)this.plugin, () -> {
            if (this.isValid(entity)) {
                consumer.accept(taskReference[0]);
                future.complete(EntityTaskResult.SUCCESS);
            } else {
                fallback.run();
                future.complete(EntityTaskResult.ENTITY_RETIRED);
            }
        }))};
        return future;
    }

    @Override
    public WrappedTask runAtEntityLater(Entity entity, @NotNull Runnable runnable, long delay) {
        return this.runAtEntityLater(entity, runnable, null, delay);
    }

    @Override
    public WrappedTask runAtEntityLater(Entity entity, @NotNull Runnable runnable, @Nullable Runnable fallback, long delay) {
        if (!this.isValid(entity)) {
            if (fallback != null) {
                fallback.run();
            }
            return null;
        }
        return this.wrapTask(this.scheduler.runTaskLater((Plugin)this.plugin, runnable, delay));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runAtEntityLater(Entity entity, @NotNull Consumer<WrappedTask> consumer, long delay) {
        return this.runAtEntityLater(entity, consumer, null, delay);
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runAtEntityLater(Entity entity, @NotNull Consumer<WrappedTask> consumer, Runnable fallback, long delay) {
        WrappedTask[] taskReference;
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        if (!this.isValid(entity) && fallback != null) {
            fallback.run();
            future.complete(null);
        }
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskLater((Plugin)this.plugin, () -> {
            consumer.accept(taskReference[0]);
            future.complete(null);
        }, delay))};
        return future;
    }

    @Override
    public WrappedTask runAtEntityLater(Entity entity, @NotNull Runnable runnable, long delay, TimeUnit unit) {
        return this.runAtEntityLater(entity, runnable, TimeConverter.toTicks(delay, unit));
    }

    @Override
    @NotNull
    public CompletableFuture<Void> runAtEntityLater(Entity entity, @NotNull Consumer<WrappedTask> consumer, long delay, TimeUnit unit) {
        return this.runAtEntityLater(entity, consumer, TimeConverter.toTicks(delay, unit));
    }

    @Override
    public WrappedTask runAtEntityTimer(Entity entity, @NotNull Runnable runnable, long delay, long period) {
        return this.runAtEntityTimer(entity, runnable, null, delay, period);
    }

    @Override
    public WrappedTask runAtEntityTimer(Entity entity, @NotNull Runnable runnable, Runnable fallback, long delay, long period) {
        if (!this.isValid(entity)) {
            if (fallback != null) {
                fallback.run();
            }
            return null;
        }
        return this.wrapTask(this.scheduler.runTaskTimer((Plugin)this.plugin, runnable, delay, period));
    }

    @Override
    public void runAtEntityTimer(Entity entity, @NotNull Consumer<WrappedTask> consumer, long delay, long period) {
        this.runAtEntityTimer(entity, consumer, null, delay, period);
    }

    @Override
    public void runAtEntityTimer(Entity entity, @NotNull Consumer<WrappedTask> consumer, Runnable fallback, long delay, long period) {
        WrappedTask[] taskReference;
        if (!this.isValid(entity) && fallback != null) {
            fallback.run();
        }
        taskReference = new WrappedTask[]{this.wrapTask(this.scheduler.runTaskTimer((Plugin)this.plugin, () -> consumer.accept(taskReference[0]), delay, period))};
    }

    @Override
    public WrappedTask runAtEntityTimer(Entity entity, @NotNull Runnable runnable, long delay, long period, TimeUnit unit) {
        return this.runAtEntityTimer(entity, runnable, TimeConverter.toTicks(delay, unit), TimeConverter.toTicks(period, unit));
    }

    @Override
    public void runAtEntityTimer(Entity entity, @NotNull Consumer<WrappedTask> consumer, long delay, long period, TimeUnit unit) {
        this.runAtEntityTimer(entity, consumer, TimeConverter.toTicks(delay, unit), TimeConverter.toTicks(period, unit));
    }

    @Override
    public void cancelTask(WrappedTask task) {
        task.cancel();
    }

    @Override
    public void cancelAllTasks() {
        this.scheduler.cancelTasks((Plugin)this.plugin);
    }

    @Override
    public List<WrappedTask> getAllTasks() {
        return this.scheduler.getPendingTasks().stream().filter(task -> task.getOwner().equals(this.plugin)).map(this::wrapTask).collect(Collectors.toList());
    }

    @Override
    public List<WrappedTask> getAllServerTasks() {
        return this.scheduler.getPendingTasks().stream().map(this::wrapTask).collect(Collectors.toList());
    }

    @Override
    public Player getPlayer(String name) {
        return this.getPlayerFromMainThread(() -> this.plugin.getServer().getPlayer(name));
    }

    @Override
    public Player getPlayerExact(String name) {
        return this.getPlayerFromMainThread(() -> this.plugin.getServer().getPlayerExact(name));
    }

    @Override
    public Player getPlayer(UUID uuid) {
        return this.getPlayerFromMainThread(() -> this.plugin.getServer().getPlayer(uuid));
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return this.teleportAsync(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        this.runAtEntity(entity, task -> {
            if (this.isValid(entity)) {
                entity.teleport(location);
                future.complete(true);
            } else {
                future.complete(false);
            }
        });
        return future;
    }

    @Override
    public WrappedTask wrapTask(Object nativeTask) {
        if (!(nativeTask instanceof BukkitTask)) {
            String nativeTaskClassName = nativeTask == null ? null : nativeTask.getClass().getName();
            throw new IllegalArgumentException("The nativeTask provided must be a BukkitTask. Got: " + nativeTaskClassName + " instead.");
        }
        return ImplementationTestsUtil.isCancelledSupported() ? new WrappedBukkitTask((BukkitTask)nativeTask) : new WrappedLegacyBukkitTask((BukkitTask)nativeTask);
    }

    private Player getPlayerFromMainThread(Supplier<Player> playerSupplier) {
        if (this.plugin.getServer().isPrimaryThread()) {
            return playerSupplier.get();
        }
        try {
            return (Player)this.scheduler.callSyncMethod((Plugin)this.plugin, playerSupplier::get).get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isValid(Entity entity) {
        if (entity.isValid()) {
            return !(entity instanceof Player) || ((Player)entity).isOnline();
        }
        return entity instanceof Projectile && !entity.isDead();
    }
}

