package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.gui.CapturedFrame;
import com.rootbeerutils.client.gui.ReconfigBridgeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into {@link Minecraft#setScreen}, {@code Minecraft.clearClientLevel} and
 * {@code Minecraft.disconnect} to:
 *
 * <ul>
 *   <li>transparently swap the vanilla {@link ServerReconfigScreen} for our
 *       {@link ReconfigBridgeScreen}, which renders the last captured in-game frame instead of
 *       the standard "Loading…" overlay,</li>
 *   <li>snapshot the framebuffer right before the world gets cleared (so the bridge screen has
 *       something to draw), and</li>
 *   <li>tear that snapshot down on disconnect.</li>
 * </ul>
 *
 * <p>We deliberately leave the real {@code TitleScreen} alone — kennytv's original mod also
 * routes the title screen through a bridge, but we want the genuine title screen with its logo,
 * panorama, and buttons intact whenever the player isn't mid-transition.
 *
 * <p>Adapted from kennytv's force-close-loading-screen mod.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftBridgeMixin {

    /**
     * Intercept screen switches and replace {@link ServerReconfigScreen} before vanilla sees it.
     * The {@code connection} field on the vanilla screen is {@code private}, so we go through
     * {@link ServerReconfigScreenAccessor} to read it without reflection.
     */
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true, name = "screen")
    private Screen rbutils$swapReconfigScreen(Screen screen) {
        if (screen instanceof ServerReconfigScreen reconfig) {
            return new ReconfigBridgeScreen(((ServerReconfigScreenAccessor) reconfig).rbutils$getConnection());
        }

        return screen;
    }

    /** Capture the framebuffer the moment before vanilla nukes the world reference. */
    @Inject(method = "clearClientLevel", at = @At("HEAD"))
    private void rbutils$captureBeforeClear(Screen screen, CallbackInfo ci) {
        CapturedFrame.captureLastFrame();
    }

    /** Tear down the captured texture on a real disconnect — we won't be needing it. */
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
    private void rbutils$clearCaptureOnDisconnect(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
        CapturedFrame.clearCapturedTexture();
    }
}
