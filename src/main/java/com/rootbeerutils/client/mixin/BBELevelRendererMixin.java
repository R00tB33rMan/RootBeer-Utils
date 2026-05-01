/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.BBE;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the per-frame culling frustum so {@code BlockVisibilityChecker} (used by the SMART
 * scheduler) can test whether a managed block entity is in view.
 */
@Mixin(LevelRenderer.class)
public class BBELevelRendererMixin {

    @Inject(at = @At("HEAD"), method = "cullTerrain", require = 0)
    private void rbutils$bbe$captureFrustum(Camera camera, Frustum frustum, boolean bl, CallbackInfo ci) {
        BBE.GlobalScope.frustum = frustum;
    }
}
