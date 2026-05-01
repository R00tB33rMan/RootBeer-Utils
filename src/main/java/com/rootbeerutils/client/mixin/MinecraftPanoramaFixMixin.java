package com.rootbeerutils.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftPanoramaFixMixin {

    @Shadow @Final public GameRenderer gameRenderer;
    @Shadow @Final private TextureManager textureManager;

    /**
     * Skip vanilla's late {@code registerPanoramaTextures} call — we already invoked it from
     * {@link #rbutils$registerPanoramaEarly}. Without this, the textures would be allocated twice,
     * leaking the first set.
     */
    @WrapWithCondition(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;registerPanoramaTextures(Lnet/minecraft/client/renderer/texture/TextureManager;)V"
        )
    )
    private boolean rbutils$skipLatePanoramaRegister(GameRenderer renderer, TextureManager textureManager) {
        return false;
    }

    /**
     * Inject the panorama registration immediately after {@code this.gameRenderer} is assigned
     * during construction. At this point the {@link TextureManager} is already initialized
     * (it's assigned a few lines earlier), so we can safely upload the panorama textures.
     */
    @Inject(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;gameRenderer:Lnet/minecraft/client/renderer/GameRenderer;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void rbutils$registerPanoramaEarly(GameConfig gameConfig, CallbackInfo ci) {
        this.gameRenderer.registerPanoramaTextures(this.textureManager);
    }
}
