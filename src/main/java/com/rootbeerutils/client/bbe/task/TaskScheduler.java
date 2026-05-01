/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.task;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadInstance;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskScheduler {

    private TaskScheduler() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("BBE-TaskScheduler");

    /**
     * Schedule a runnable on the main Minecraft thread.
     */
    public static void schedule(@NonNull Runnable task) {
        Minecraft.getInstance().execute(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                LOGGER.error("Scheduled task failed", t);
            }
        });
    }

    /**
     * Schedule a runnable on the main thread once the given resource reload completes
     * <em>without</em> errors. Errors are logged and swallowed.
     */
    public static void scheduleOnReload(ReloadInstance reload, @NonNull Runnable task) {
        if (reload == null) {
            LOGGER.error("Resource reload instance was null");
            return;
        }

        reload.done().whenComplete((ignored, err) -> {
            if (err != null) {
                LOGGER.error("Resource reload future completed exceptionally", err);
                return;
            }

            try {
                reload.checkExceptions();
            } catch (Throwable t) {
                LOGGER.error("Resource reload finished with errors", t);
                return;
            }

            Minecraft.getInstance().execute(() -> {
                try {
                    task.run();
                } catch (Throwable t) {
                    LOGGER.error("Post-reload task failed", t);
                }
            });
        });
    }
}
