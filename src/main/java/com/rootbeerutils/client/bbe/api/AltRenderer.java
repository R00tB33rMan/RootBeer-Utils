/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.api;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Public API: addon mods implement this to plug a custom renderer for a supported block-entity
 * type. Provided through an {@link AltRendererProvider} on the {@code bbe:renderer_registration_api}
 * Fabric entrypoint.
 */
public interface AltRenderer<T extends BlockEntity, S extends BlockEntityRenderState> {
    S createRenderState();

    default void extractRenderState(final T blockEntity,
                                    final S state,
                                    final ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
    }

    void submit(final S state,
                final PoseStack poseStack,
                final SubmitNodeCollector submitNodeCollector,
                final CameraRenderState camera);

    default int getViewDistance() {
        return 64;
    }

    default boolean shouldRender(final T blockEntity, final Vec3 cameraPosition) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPosition, this.getViewDistance());
    }

    /**
     * Returns true if this renderer fully replaces vanilla's block-entity renderer for the matched
     * type. Currently advisory only — the chunk-mesh substitution that would let vanilla be
     * skipped without leaving an invisible block ship in a follow-up phase.
     */
    default boolean dedicatedRenderer() {
        return false;
    }
}
