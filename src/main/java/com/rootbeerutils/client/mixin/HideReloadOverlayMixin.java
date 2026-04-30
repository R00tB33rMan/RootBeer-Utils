package com.rootbeerutils.client.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Replaces the reload-overlay render with a one-shot completion path: when the underlying
 * {@link ReloadInstance} is done we forward to the queued {@code onFinish} consumer, hand
 * control back to the underlying screen if any, and clear the overlay — without ever drawing
 * the (now-irrelevant) progress bar over the player's view.
 *
 * <p>For a fade-out path that's already running we preserve vanilla's "wait for fade out before
 * clearing" sequencing so cinematic transitions still complete cleanly.
 */
@Mixin(LoadingOverlay.class)
public class HideReloadOverlayMixin {

    @Shadow @Final private boolean fadeIn;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private ReloadInstance reload;
    @Shadow @Final private Consumer<Optional<Throwable>> onFinish;
    @Shadow private long fadeOutStart;

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void rbutils$skipReloadRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                          float a, CallbackInfo ci) {
        // Fade-out already in progress (vanilla path) → leave alone, only finish quietly.
        if (!this.fadeIn) {
            if (this.fadeOutStart != -1L) {
                this.minecraft.setOverlay(null);
                ci.cancel();
            }

            return;
        }

        // Let the active screen still render normally underneath us.
        if (this.minecraft.level == null && this.minecraft.screen != null) {
            this.minecraft.screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, a);
        }

        if (this.reload.isDone()) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable t) {
                this.onFinish.accept(Optional.of(t));
            }

            // Re-init the screen so resized viewports lay out correctly post-reload.
            if (this.minecraft.screen != null) {
                Window window = this.minecraft.getWindow();
                this.minecraft.screen.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
            }

            this.minecraft.setOverlay(null);
        }

        ci.cancel();
    }
}
