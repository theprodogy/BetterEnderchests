/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.folialib.util;

import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

public class InvalidTickDelayNotifier {
    public static boolean disableNotifications = false;
    public static boolean debugMode = false;
    private static boolean notified = false;

    public static void notifyOnce(@NotNull Logger logger, long span) {
        if (notified || disableNotifications) {
            return;
        }
        notified = true;
        logger.warning(String.format("A tick based delay or timer was scheduled with a time span of %d ticks. ", span) + "The server is already processing the current tick and, as such, won't process new tasks in less than 1 tick. FoliaLib will automatically change time spans of <= 0 ticks to 1 tick going forward. This warning is purely informative and won't impact the operation of the plugin. Plugin developers can disable this warning or enable debug mode to location the source of this warning.");
        if (debugMode) {
            new Exception("Debugging information to track down the location of the invalid tick input value").printStackTrace();
        }
    }
}

