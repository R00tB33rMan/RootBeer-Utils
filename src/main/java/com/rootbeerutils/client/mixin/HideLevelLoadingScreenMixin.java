package com.rootbeerutils.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the level-loading screen render once a world is already loaded — useful when the
 * screen briefly retriggers during teleporting or dimension transitions and would otherwise flash
 * the loading background over already-visible terrain.
 */
@Mixin(LevelLoadingScreen.class)
public class HideLevelLoadingScreenMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void rbutils$skipRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                    float a, CallbackInfo ci) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        ci.cancel();
    }

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void rbutils$skipBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                        float a, CallbackInfo ci) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        ci.cancel();
    }
}
