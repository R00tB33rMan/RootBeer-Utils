package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.zoom.ZoomClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the held item while Zoom is active, so the hand doesn't fill the zoomed frame.
 */
@Mixin(GameRenderer.class)
public class ZoomGameRendererMixin {

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void rbutils$hideHandWhileZooming(CameraRenderState cameraState,
                                              float deltaPartialTick,
                                              Matrix4fc modelViewMatrix,
                                              CallbackInfo ci) {
        if (!ZoomClient.isZoomActive()) {
            return;
        }

        ci.cancel();
    }
}
