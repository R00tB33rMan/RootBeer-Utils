/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.ext.BlockEntityRenderStateExt;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntityRenderState.class)
public class BBEBlockEntityRenderStateMixin implements BlockEntityRenderStateExt {

    @Unique private BlockEntity rbutils$blockEntity;

    @Override
    public void rootbeer_utils$blockEntity(BlockEntity blockEntity) {
        this.rbutils$blockEntity = blockEntity;
    }

    @Override
    public BlockEntity rootbeer_utils$blockEntity() {
        return this.rbutils$blockEntity;
    }
}
