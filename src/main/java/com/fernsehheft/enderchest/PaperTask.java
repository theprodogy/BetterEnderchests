package com.fernsehheft.enderchest;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

/** Small cancellation handle for scheduled plugin work. */
final class PaperTask {
    private final ScheduledTask task;

    PaperTask(ScheduledTask task) {
        this.task = task;
    }

    void cancel() {
        this.task.cancel();
    }
}
