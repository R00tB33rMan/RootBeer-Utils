/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.ext.BlockEntityExt;
import com.rootbeerutils.client.bbe.ext.RenderingMode;

import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public abstract class BBEBlockEntityMixin implements BlockEntityExt {

    @Unique private RenderingMode rbutils$renderingMode = RenderingMode.TERRAIN;
    @Unique private boolean rbutils$terrainMeshReady = true;
    @Unique private boolean rbutils$supportedBlockEntity = false;
    @Unique private byte rbutils$bbeKind = 0;

    @Override
    public boolean rootbeer_utils$supportedBlockEntity() {
        return rbutils$supportedBlockEntity;
    }

    @Override
    public void rootbeer_utils$supportedBlockEntity(boolean bl) {
        rbutils$supportedBlockEntity = bl;
    }

    @Override
    public RenderingMode rootbeer_utils$renderingMode() {
        return rbutils$renderingMode;
    }

    @Override
    public void rootbeer_utils$renderingMode(RenderingMode mode) {
        rbutils$renderingMode = mode;
    }

    @Override
    public boolean rootbeer_utils$terrainMeshReady() {
        return rbutils$terrainMeshReady;
    }

    @Override
    public void rootbeer_utils$terrainMeshReady(boolean bl) {
        rbutils$terrainMeshReady = bl;
    }

    @Override
    public void rootbeer_utils$hasSpecialManager(boolean bl) {
    }

    @Override
    public byte rootbeer_utils$optKind() {
        return rbutils$bbeKind;
    }

    @Override
    public void rootbeer_utils$optKind(byte k) {
        rbutils$bbeKind = k;
    }
}
