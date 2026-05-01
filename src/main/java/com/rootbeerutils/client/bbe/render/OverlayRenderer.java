/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.rootbeerutils.client.bbe.config.ConfigCache;
import com.rootbeerutils.client.bbe.ext.BlockEntityExt;
import com.rootbeerutils.client.bbe.ext.RenderingMode;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class OverlayRenderer {

    private OverlayRenderer() {
    }

    public static <S> boolean manageCrumblingOverlay(BlockEntity blockEntity, PoseStack poseStack, Model<? super S> model,
                                                     S state, int light, int overlayCoords, int tint,
                                                     ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (crumblingOverlay == null) {
            return false;
        }

        BlockEntityExt blockEntityExt = (BlockEntityExt) blockEntity;

        boolean substitutionActive =
                ConfigCache.masterOptimize
                        && ConfigCache.ENABLED[blockEntityExt.rootbeer_utils$optKind() & 0xFF];

        if (substitutionActive
                && blockEntityExt.rootbeer_utils$renderingMode() == RenderingMode.TERRAIN
                && blockEntityExt.rootbeer_utils$terrainMeshReady()) {
            OverlayNodeCollection.submitCrumblingOverlay(poseStack, model, state, light, overlayCoords, tint, crumblingOverlay);
            return true;
        }

        return false;
    }

    public static <S> void submitCrumblingOverlay(PoseStack poseStack, Model<? super S> model, S state,
                                                  int light, int overlayCoords, int tint,
                                                  ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        OverlayNodeCollection.submitCrumblingOverlay(poseStack, model, state, light, overlayCoords, tint, crumblingOverlay);
    }

    /**
     * True if any destruction-progress entry exists for the given block-pos packed long.
     */
    public static boolean isBreaking(long posLong, Long2ObjectMap<?> progression) {
        return !progression.isEmpty() && progression.get(posLong) != null;
    }

    public static void renderCrumblingOverlays(MultiBufferSource.BufferSource crumblingBufferSource, PoseStack poseStack) {
        for (OverlayNodeCollection.OverlaySubmit<?> submit : OverlayNodeCollection.getSubmits()) {
            renderSubmit(crumblingBufferSource, poseStack, submit);
        }

        OverlayNodeCollection.clearSubmits();
    }

    private static <S> void renderSubmit(MultiBufferSource.BufferSource crumblingBufferSource, PoseStack poseStack,
                                         OverlayNodeCollection.OverlaySubmit<S> submit) {
        poseStack.pushPose();
        poseStack.last().set(submit.poseStack());

        renderCrumblingOverlay(
                crumblingBufferSource,
                submit.crumblingOverlay(),
                submit.model(),
                submit.state(),
                poseStack,
                submit.lightCoords(),
                submit.overlayCoords(),
                submit.tintedColor()
        );

        poseStack.popPose();
    }

    private static <S> void renderCrumblingOverlay(MultiBufferSource.BufferSource crumblingBufferSource,
                                                   ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
                                                   Model<? super S> model, S state, PoseStack poseStack,
                                                   int i, int j, int k) {
        VertexConsumer crumblingBuffer = new SheetedDecalTextureGenerator(
                crumblingBufferSource.getBuffer(ModelBakery.DESTROY_TYPES.get(crumblingOverlay.progress())),
                crumblingOverlay.cameraPose(),
                1.0F
        );

        if (state != null) {
            model.setupAnim(state);
        }

        model.renderToBuffer(poseStack, crumblingBuffer, i, j, k);
    }
}
