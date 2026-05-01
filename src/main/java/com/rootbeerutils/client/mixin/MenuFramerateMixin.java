package com.rootbeerutils.client.mixin;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.rootbeerutils.client.framerate.MonitorRefreshRate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public abstract class MenuFramerateMixin {

    @Shadow private int framerateLimit;

    @Shadow public abstract FramerateLimitTracker.FramerateThrottleReason getThrottleReason();

    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void rbutils$capMenuToMonitorRefresh(CallbackInfoReturnable<Integer> cir) {
        if (this.getThrottleReason() != FramerateLimitTracker.FramerateThrottleReason.OUT_OF_LEVEL_MENU) {
            return;
        }

        int monitorRate = MonitorRefreshRate.forActiveWindow();
        cir.setReturnValue(Math.min(this.framerateLimit, monitorRate));
    }
}
