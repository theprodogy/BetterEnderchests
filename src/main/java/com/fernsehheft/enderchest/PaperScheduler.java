package com.fernsehheft.enderchest;

import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/** Native Paper scheduler facade that is safe on both Paper and Folia. */
final class PaperScheduler {
    private final JavaPlugin plugin;

    PaperScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    PaperScheduler getScheduler() {
        return this;
    }

    boolean isFolia() {
        return ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));
    }

    PaperTask runNextTick(Consumer<PaperTask> action) {
        ScheduledTask task = this.plugin.getServer().getGlobalRegionScheduler().run(this.plugin, scheduled -> action.accept(new PaperTask(scheduled)));
        return new PaperTask(task);
    }

    PaperTask runLater(Runnable action, long delayTicks) {
        ScheduledTask task = this.plugin.getServer().getGlobalRegionScheduler().runDelayed(this.plugin, scheduled -> action.run(), delayTicks);
        return new PaperTask(task);
    }

    PaperTask runTimer(Runnable action, long delayTicks, long periodTicks) {
        ScheduledTask task = this.plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(this.plugin, scheduled -> action.run(), delayTicks, periodTicks);
        return new PaperTask(task);
    }

    PaperTask runAsync(Consumer<PaperTask> action) {
        ScheduledTask task = this.plugin.getServer().getAsyncScheduler().runNow(this.plugin, scheduled -> action.accept(new PaperTask(scheduled)));
        return new PaperTask(task);
    }

    PaperTask runLaterAsync(Runnable action, long delayTicks) {
        ScheduledTask task = this.plugin.getServer().getAsyncScheduler().runDelayed(this.plugin, scheduled -> action.run(), delayTicks * 50L, TimeUnit.MILLISECONDS);
        return new PaperTask(task);
    }

    CompletableFuture<Void> runAtEntity(Entity entity, Consumer<PaperTask> action) {
        CompletableFuture<Void> completed = new CompletableFuture<>();
        entity.getScheduler().run(this.plugin, scheduled -> {
            try {
                action.accept(new PaperTask(scheduled));
                completed.complete(null);
            } catch (Throwable throwable) {
                completed.completeExceptionally(throwable);
            }
        }, () -> completed.complete(null));
        return completed;
    }

    PaperTask runAtEntityLater(Entity entity, Runnable action, long delayTicks) {
        ScheduledTask task = entity.getScheduler().runDelayed(this.plugin, scheduled -> action.run(), () -> { }, delayTicks);
        return new PaperTask(task);
    }
}
