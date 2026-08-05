package com.rootbeerutils.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.rootbeerutils.client.appleskin.client.HUDOverlayHandler;

@Mixin(Minecraft.class)
public class AppleSkinMinecraftMixin
{
    @Inject(at = @At("HEAD"), method = "tick")
    void onTick(CallbackInfo info)
    {
        if (HUDOverlayHandler.INSTANCE != null)
            HUDOverlayHandler.INSTANCE.onClientTick();
    }
}
