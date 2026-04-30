package com.rootbeerutils.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips the title-screen fade-in by clearing the {@code fading} flag at extract time.
 */
@Mixin(TitleScreen.class)
public class HideTitleScreenFadeMixin {

    @Shadow private boolean fading;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void rbutils$skipFade(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                  float a, CallbackInfo ci) {
        this.fading = false;
    }
}
