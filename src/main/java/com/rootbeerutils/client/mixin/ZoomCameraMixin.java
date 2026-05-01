package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.zoom.ZoomClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class ZoomCameraMixin {

    @Inject(method = "calculateFov(F)F", at = @At("RETURN"), cancellable = true)
    private void rbutils$zoomHeldFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (!ZoomClient.isZoomActive()) {
            return;
        }

        cir.setReturnValue(ZoomClient.ZOOM_HOLD_FOV);
    }
}
