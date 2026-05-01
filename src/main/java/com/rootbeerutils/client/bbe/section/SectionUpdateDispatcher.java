/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.section;

import com.rootbeerutils.client.bbe.task.TaskScheduler;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SectionUpdateDispatcher {

    private SectionUpdateDispatcher() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("BBE-SectionUpdateDispatcher");

    public static void queueRebuildAtBlockPos(BlockPos pos) {
        try {
            TaskScheduler.schedule(() -> {
                Level level = Minecraft.getInstance().level;
                if (level == null) {
                    return;
                }

                BlockState state = level.getBlockState(pos);
                Minecraft.getInstance().levelRenderer.blockChanged(level, pos, state, state, 8);
            });
        } catch (Exception e) {
            LOGGER.error("Failed to rebuild terrain section!", e);
            SectionRebuildCallbacks.remove(pos);
        }
    }

    /**
     * Rebuild a section with a fence callback (runs after section rebuild is complete).
     */
    public static void queueRebuildAtBlockPos(BlockPos pos, Runnable onUploadedFence) {
        SectionRebuildCallbacks.await(pos, onUploadedFence);
        queueRebuildAtBlockPos(pos);
    }

    public static void queueUpdateAllSections() {
        try {
            Minecraft.getInstance().levelRenderer.allChanged();
        } catch (Exception e) {
            LOGGER.error("Reloading terrain sections failed!", e);
        }
    }
}
