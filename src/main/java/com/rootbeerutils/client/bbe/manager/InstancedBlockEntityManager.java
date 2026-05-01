/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.manager;

import com.rootbeerutils.client.bbe.BBE;
import com.rootbeerutils.client.bbe.api.AltRenderers;
import com.rootbeerutils.client.bbe.config.ConfigCache;
import com.rootbeerutils.client.bbe.config.EnumTypes;
import com.rootbeerutils.client.bbe.ext.BlockEntityExt;
import com.rootbeerutils.client.bbe.ext.RenderingMode;
import com.rootbeerutils.client.bbe.section.SectionUpdateDispatcher;
import com.rootbeerutils.client.bbe.task.ManagerTasks;
import com.rootbeerutils.client.bbe.util.BlockVisibilityChecker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Per-block-entity state machine driving the IDLE → IMMEDIATE → WAITING_TERRAIN cycle. When a BE
 * starts animating it requests IMMEDIATE rendering (queues a chunk rebuild that drops the BE's
 * geometry from the terrain mesh and lets the BBE BER take over); when animation stops it
 * requests TERRAIN rendering (queues a rebuild that re-bakes geometry into the terrain mesh and
 * waits for the upload fence before allowing the BER to be cancelled).
 */
public final class InstancedBlockEntityManager {
    private enum Phase {
        IDLE,
        IMMEDIATE_ACTIVE,
        WAITING_TERRAIN
    }

    private boolean queued = false;

    private final BlockEntity blockEntity;
    private final BlockEntityExt ext;
    private final BlockPos pos;

    private boolean animating;
    private boolean durationTask;
    private float durationTaskStart;
    private float duration;

    private Phase phase = Phase.IDLE;

    public InstancedBlockEntityManager(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.ext = (BlockEntityExt) blockEntity;
        this.pos = blockEntity.getBlockPos();
    }

    public boolean tryMarkQueued() {
        if (queued) return false;
        queued = true;
        return true;
    }

    public void clearQueued() {
        queued = false;
    }

    public boolean isAnimating() {
        return animating;
    }

    private void setAnimating(boolean animating) {
        this.animating = animating;
        if (animating && phase == Phase.WAITING_TERRAIN) {
            enterImmediate();
        }
    }

    public void setDurationTask(boolean enabled, float start, float duration) {
        this.durationTask = enabled;
        this.durationTaskStart = start;
        this.duration = duration;
        if (enabled && phase == Phase.WAITING_TERRAIN) {
            enterImmediate();
        }
    }

    public boolean isValid() {
        return !(blockEntity == null || blockEntity.isRemoved());
    }

    public void forceKill() {
        this.animating = false;
        this.durationTaskStart = 0;
        this.duration = 0;
        this.phase = Phase.IDLE;
        this.queued = false;
    }

    public int run() {
        if (!ext.rootbeer_utils$supportedBlockEntity()
                || !ConfigCache.ENABLED[ext.rootbeer_utils$optKind() & 0xFF]
                || !ConfigCache.masterOptimize
                || AltRenderers.hasRendererOverride(blockEntity.getType())) {
            phase = Phase.IDLE;
            return ManagerTasks.FINISHED;
        }

        switch (phase) {
            case IDLE -> {
                if (shouldBeImmediate()) {
                    enterImmediate();
                    return ManagerTasks.PROCESSING;
                }
                return ManagerTasks.FINISHED;
            }
            case IMMEDIATE_ACTIVE -> {
                if (shouldBeImmediate()) return ManagerTasks.PROCESSING;
                requestTerrainFence();
                return ManagerTasks.PROCESSING;
            }
            case WAITING_TERRAIN -> {
                return ManagerTasks.FINISHED;
            }
        }

        return ManagerTasks.FINISHED;
    }

    public void tick(boolean animState, boolean animOption) {
        if (!animOption) {
            this.setAnimating(false);
            return;
        }
        boolean old = this.isAnimating();
        this.setAnimating(animState);
        if (old != animState) {
            ManagerTasks.schedule(this);
        }
    }

    public void trigger(float start, float duration, boolean animOption) {
        if (!animOption) {
            this.setAnimating(false);
            return;
        }
        this.setDurationTask(true, start, duration);
        ManagerTasks.schedule(this);
    }

    private boolean shouldBeImmediate() {
        if (animating) return true;
        if (durationTask && isDurationStillRunning()) return true;
        if (isSmartSchedulerEnabled() && isVisibleInFov()) return true;
        return false;
    }

    private boolean isDurationStillRunning() {
        if (blockEntity.getLevel() == null) return false;
        float now = blockEntity.getLevel().getGameTime();
        return (now - durationTaskStart) <= duration;
    }

    private void enterImmediate() {
        phase = Phase.IMMEDIATE_ACTIVE;
        ext.rootbeer_utils$terrainMeshReady(false);
        if (ext.rootbeer_utils$renderingMode() != RenderingMode.IMMEDIATE) {
            ext.rootbeer_utils$renderingMode(RenderingMode.IMMEDIATE);
            SectionUpdateDispatcher.queueRebuildAtBlockPos(pos);
        }
    }

    private void requestTerrainFence() {
        phase = Phase.WAITING_TERRAIN;
        if (ext.rootbeer_utils$renderingMode() != RenderingMode.TERRAIN) {
            ext.rootbeer_utils$renderingMode(RenderingMode.TERRAIN);
        }
        ext.rootbeer_utils$terrainMeshReady(false);
        SectionUpdateDispatcher.queueRebuildAtBlockPos(pos, () -> {
            if (!ConfigCache.ENABLED[ext.rootbeer_utils$optKind() & 0xFF] || !ConfigCache.masterOptimize) {
                ext.rootbeer_utils$terrainMeshReady(true);
                phase = Phase.IDLE;
                return;
            }
            if (shouldBeImmediate()) {
                enterImmediate();
                ManagerTasks.schedule(this);
                return;
            }
            ext.rootbeer_utils$terrainMeshReady(true);
            phase = Phase.IDLE;
        });
    }

    private boolean isSmartSchedulerEnabled() {
        return ConfigCache.updateType == EnumTypes.UpdateSchedulerType.SMART.ordinal();
    }

    private boolean isVisibleInFov() {
        return BlockVisibilityChecker.isBlockInFOVAndVisible(BBE.GlobalScope.frustum, blockEntity)
                == BlockVisibilityChecker.Visibility.VISIBLE;
    }

    /** Identifiers for the dense {@link ConfigCache#ENABLED} table; mirrored on {@code BlockEntityExt.optKind}. */
    public static final class OptKind {
        private OptKind() { }
        public static final byte NONE     = 0;
        public static final byte CHEST    = 1;
        public static final byte SIGN     = 2;
        public static final byte BED      = 3;
        public static final byte SHULKER  = 4;
        public static final byte POT      = 5;
        public static final byte BANNER   = 6;
        public static final byte BELL     = 7;
        public static final byte CGS      = 8;
        public static final byte SHELF    = 9;
        public static final byte CAMPFIRE = 10;
    }
}
