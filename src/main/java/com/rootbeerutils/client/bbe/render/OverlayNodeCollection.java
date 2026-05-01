/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;

import java.util.ArrayList;
import java.util.List;

public final class OverlayNodeCollection {

    private OverlayNodeCollection() {
    }

    private static final List<OverlaySubmit<?>> submits = new ArrayList<>();

    public static <S> void submitCrumblingOverlay(PoseStack poseStack, Model<? super S> model, S state,
                                                  int light, int overlayCoords, int tint,
                                                  ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        OverlaySubmit<?> overlaySubmit = new OverlaySubmit<>(
                poseStack.last().copy(), model, state, light, overlayCoords, tint, crumblingOverlay);
        submits.add(overlaySubmit);
    }

    public static List<OverlaySubmit<?>> getSubmits() {
        return submits;
    }

    public static void clearSubmits() {
        submits.clear();
    }

    public record OverlaySubmit<S>(PoseStack.Pose poseStack,
                                   Model<? super S> model,
                                   S state,
                                   int lightCoords,
                                   int overlayCoords,
                                   int tintedColor,
                                   ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
    }
}
