/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.mixin;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.MipmapGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MipmapGenerator.class)
public interface MipMapGeneratorAccessor {

    @Invoker("scaleAlphaToCoverage")
    static void scaleAlphaToCoverageInvoke(NativeImage image, float h, float g, float f) {
        throw new AssertionError();
    }
}
