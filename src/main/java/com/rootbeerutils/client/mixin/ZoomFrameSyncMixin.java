package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.zoom.ZoomClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ZoomFrameSyncMixin {

    @Inject(method = "runTick", at = @At("HEAD"))
    private void rbutils$syncZoomBeforeInput(boolean advanceGameTime, CallbackInfo ci) {
        ZoomClient.onFrameStart();
    }
}
