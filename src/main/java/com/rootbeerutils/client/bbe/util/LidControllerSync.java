/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.util;

import com.rootbeerutils.client.bbe.config.ConfigCache;
import com.rootbeerutils.client.bbe.ext.BlockEntityExt;
import com.rootbeerutils.client.bbe.ext.RenderingMode;
import com.rootbeerutils.client.bbe.section.SectionUpdateDispatcher;
import com.rootbeerutils.client.mixin.ChestBlockEntityAccessor;
import com.rootbeerutils.client.mixin.ChestLidControllerAccessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;

public final class LidControllerSync {

    private LidControllerSync() {
    }

    public static void sync(ClientLevel clientLevel, BlockPos blockPos, BlockState blockState) {
        if (!ConfigCache.optimizeChests || !ConfigCache.chestAnims || !clientLevel.isClientSide()) {
            return;
        }

        Block block = blockState.getBlock();
        if (!(block instanceof ChestBlock)) {
            return;
        }

        BlockEntity blockEntity = tryGetBlockEntity(clientLevel, blockPos);
        ChestBlockEntity opposite = BlockVisibilityChecker.getOtherChestHalf(clientLevel, blockPos);

        if (blockEntity == null || opposite == null || !(opposite.getOpenNess(0.5f) > 0f)) {
            return;
        }

        ChestLidController src = ((ChestBlockEntityAccessor) opposite).getLidController();
        ChestLidController dst = ((ChestBlockEntityAccessor) blockEntity).getLidController();

        ChestLidControllerAccessor accSrc = (ChestLidControllerAccessor) src;
        ChestLidControllerAccessor accDst = (ChestLidControllerAccessor) dst;

        accDst.setOpen(accSrc.getOpen());
        accDst.setProgress(accSrc.getProgress());
        accDst.setLastProgress(accSrc.getLastProgress());

        // Wake up the BE ticker (Lithium workaround — calling blockEvent triggers a tick).
        clientLevel.blockEvent(blockPos, blockState.getBlock(), 1, 0);

        BlockEntityExt oppositeExt = (BlockEntityExt) opposite;
        if (oppositeExt.rootbeer_utils$renderingMode() == RenderingMode.IMMEDIATE) {
            BlockEntityExt blockEntityExt = (BlockEntityExt) blockEntity;
            blockEntityExt.rootbeer_utils$terrainMeshReady(false);
            blockEntityExt.rootbeer_utils$renderingMode(RenderingMode.IMMEDIATE);
            SectionUpdateDispatcher.queueRebuildAtBlockPos(blockPos);
        }
    }

    private static BlockEntity tryGetBlockEntity(Level level, BlockPos pos) {
        try {
            return level.getBlockEntity(pos);
        } catch (Exception e) {
            return null;
        }
    }
}
