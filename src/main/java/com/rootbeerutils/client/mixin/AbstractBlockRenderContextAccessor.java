/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 *
 * Accessor mixin for VulkanMod's {@code AbstractBlockRenderContext}, which holds the protected
 * state fields that {@link BBEVulkanBlockRendererMixin} reads from. We can't @Shadow these
 * directly from a mixin into {@code BlockRenderer} (the subclass) because Mixin's annotation
 * processor only resolves field shadows against the direct target class.
 */
package com.rootbeerutils.client.mixin;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "net.vulkanmod.render.chunk.build.renderer.AbstractBlockRenderContext", remap = false)
public interface AbstractBlockRenderContextAccessor {

    @Accessor BlockState  getBlockState();
    @Accessor BlockPos    getBlockPos();
    @Accessor BlockAndTintGetter getRenderRegion();
    @Accessor RandomSource getRandom();

    @Invoker QuadEmitter invokeGetEmitter();
    @Invoker boolean     invokeIsFaceCulled(Direction direction);
}
