package com.rootbeerutils.client.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LoadingOverlay.class)
public class HideReloadOverlayMixin {

    @Shadow @Final private boolean fadeIn;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private ReloadInstance reload;
    @Shadow @Final private Consumer<Optional<Throwable>> onFinish;
    @Shadow private long fadeOutStart;

    /**
     * Latched true once the very first reload of this JVM has completed. Distinguishes the
     * boot reload (which we have to detect by ordinal, since 26.1 happens to use the same
     * {@code fadeIn=false} flag for it as for non-cinematic reloads) from every later reload.
     */
    @Unique
    private static boolean rbutils$pastBootReload = false;

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void rbutils$skipReloadRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                          float a, CallbackInfo ci) {
        // Boot reload: drive the lifecycle silently, never render. Vanilla still renders mc.screen
        // (which MinecraftSkipBootScreenMixin makes a logo-less, fade-less TitleScreen) through
        // the regular screen path, so the player just sees the panorama + buttons until the
        // reload finishes and onFinish flips us to the real interactive title screen.
        if (!rbutils$pastBootReload) {
            if (this.reload.isDone()) {
                rbutils$drainLifecycle();
                rbutils$pastBootReload = true;
            }
            ci.cancel();
            return;
        }

        // Cinematic-reload short-circuit: a vanilla path is mid-fade-out → kill the overlay so the
        // remaining fade frames never draw. fadeOutStart still being -1 means vanilla hasn't
        // started fading yet, in which case we let vanilla's body run normally.
        if (!this.fadeIn) {
            if (this.fadeOutStart != -1L) {
                this.minecraft.setOverlay(null);
                ci.cancel();
            }
            return;
        }

        // Subsequent reload (F3+T, pack apply, pack unload): keep whatever the player was
        // looking at visible underneath, then drive the lifecycle when the reload is done.
        if (this.minecraft.level == null && this.minecraft.screen != null) {
            this.minecraft.screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, a);
        }

        if (this.reload.isDone()) {
            rbutils$drainLifecycle();
        }

        ci.cancel();
    }

    /**
     * Forwards completion to the queued {@code onFinish} consumer (which sets up the post-reload
     * screen — typically a {@link net.minecraft.client.gui.screens.TitleScreen}), re-initializes
     * that screen against the current window dimensions, and disposes the overlay.
     */
    @Unique
    private void rbutils$drainLifecycle() {
        try {
            this.reload.checkExceptions();
            this.onFinish.accept(Optional.empty());
        } catch (Throwable t) {
            this.onFinish.accept(Optional.of(t));
        }

        if (this.minecraft.screen != null) {
            Window window = this.minecraft.getWindow();
            this.minecraft.screen.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
        }

        this.minecraft.setOverlay(null);
    }
}
