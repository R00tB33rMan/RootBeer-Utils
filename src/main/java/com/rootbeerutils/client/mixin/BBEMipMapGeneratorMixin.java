/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.mixin;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MipmapGenerator.class)
public abstract class BBEMipMapGeneratorMixin {

    @Redirect(method = "generateMipLevels(Lnet/minecraft/resources/Identifier;[Lcom/mojang/blaze3d/platform/NativeImage;ILnet/minecraft/client/renderer/texture/MipmapStrategy;FLcom/mojang/blaze3d/platform/Transparency;)[Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;scaleAlphaToCoverage(Lcom/mojang/blaze3d/platform/NativeImage;FFF)V"),
            require = 0
    )
    private static void rbutils$bbe$skipEntityTextureAlphaCoverage(NativeImage image, float h, float g, float f, Identifier identifier) {
        if (rbutils$bbe$shouldScale(identifier)) {
            MipMapGeneratorAccessor.scaleAlphaToCoverageInvoke(image, h, g, f);
        }
    }

    @Unique
    private static boolean rbutils$bbe$shouldScale(Identifier identifier) {
        return identifier == null || !identifier.getPath().contains("entity");
    }
}
