package com.rootbeerutils.client.bbe.render.bers;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.*;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.NonNull;

public class BBEDummyRenderer implements BlockEntityRenderer<BlockEntity, BlockEntityRenderState> {

    public @NonNull BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    public void submit(@NonNull BlockEntityRenderState state,
                       @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector submitNodeCollector,
                       @NonNull CameraRenderState camera) {
    }

    public void extractRenderState(final @NonNull BlockEntity blockEntity,
                                   final @NonNull BlockEntityRenderState state,
                                   final float partialTicks,
                                   final @NonNull Vec3 cameraPosition,
                                   final ModelFeatureRenderer.CrumblingOverlay breakProgress) {
    }

    @Override
    public int getViewDistance() {
        return 0;
    }
}
