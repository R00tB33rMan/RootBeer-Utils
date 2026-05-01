/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 *
 * Pseudo mixin against VulkanMod's chunk-rendering pipeline. Replaces what BBE did via two
 * separate Sodium mixins (extract + LevelRenderer.submitBlockEntities) with a single hook into
 * VulkanMod's combined extract-and-submit path.
 */
package com.rootbeerutils.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import com.mojang.blaze3d.vertex.PoseStack;

import com.rootbeerutils.client.bbe.BBE;
import com.rootbeerutils.client.bbe.api.AltRenderDispatcher;
import com.rootbeerutils.client.bbe.api.AltRenderers;
import com.rootbeerutils.client.bbe.config.BBEGameOptions;
import com.rootbeerutils.client.bbe.config.ConfigCache;
import com.rootbeerutils.client.bbe.render.OverlayRenderer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.SortedSet;

@Pseudo
@Mixin(targets = "net.vulkanmod.render.chunk.WorldRenderer", remap = false)
public class BBEVulkanWorldRendererMixin {

    /**
     * Update the dispatcher with this frame's camera position before the per-section loop runs.
     */
    @Inject(method = "renderBlockEntities", at = @At("HEAD"), remap = false, require = 0)
    private void rbutils$bbe$prepareDispatcher(PoseStack poseStack,
                                               LevelRenderState levelRenderState,
                                               SubmitNodeStorage submitNodeStorage,
                                               Long2ObjectMap<SortedSet<BlockDestructionProgress>> progression,
                                               CallbackInfo ci) {
        // Only suppress vanilla sign-body rendering when our chunk-mesh substitution is actually
        // emitting that geometry. Otherwise, the body is in neither place → invisible signs.
        BBE.GlobalScope.limitVanillaSignRendering =
                ConfigCache.masterOptimize && ConfigCache.optimizeSigns;

        AltRenderDispatcher dispatcher = BBE.GlobalScope.altRenderDispatcher;
        if (dispatcher == null) {
            return;
        }

        Vec3 camPos = levelRenderState.cameraRenderState.pos;
        dispatcher.prepare(camPos);
    }

    /**
     * Wraps the per-section {@code tryExtractRenderState} call (ordinal 0). Dispatches alt
     * renderers when enabled, optionally skips vanilla extraction for "dedicated" alt-rendered
     * types — but defaults to additive behavior, so we don't leave invisible blocks behind.
     */
    @WrapOperation(
            method = "renderBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;",
                    ordinal = 0
            ),
            remap = false,
            require = 0
    )
    private BlockEntityRenderState rbutils$bbe$wrapSectionExtract(BlockEntityRenderDispatcher dispatcher,
                                                                  BlockEntity be,
                                                                  float partialTicks,
                                                                  ModelFeatureRenderer.CrumblingOverlay overlay,
                                                                  Operation<BlockEntityRenderState> op,
                                                                  @Local(argsOnly = true, name = "destructionProgress") Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress) {
        BBEGameOptions options = BBE.GlobalScope.options;
        AltRenderDispatcher altDispatcher = BBE.GlobalScope.altRenderDispatcher;

        if (!options.optimizations.enabled || altDispatcher == null) {
            return op.call(dispatcher, be, partialTicks, overlay);
        }

        // Run any alt renderers that match this block entity, accumulating their render states
        // into the per-frame buffer that the world-renderer mixin's RETURN hook submits later.
        List<BlockEntityRenderState> altStates = altDispatcher.tryExtractRenderStates(be, overlay);
        for (BlockEntityRenderState s : altStates) {
            if (s != null) {
                BBE.GlobalScope.altBlockEntityRenderStates.add(s);
            }
        }

        // Optionally, skip vanilla extraction for types that have a dedicated alt renderer. Without
        // chunk-mesh substitution this leaves no terrain mesh either => invisible block, so it's
        // gated behind an explicit user opt-in via the BBE options menu.
        if (options.optimizations.skipVanillaForDedicated
                && AltRenderers.hasRendererOverride(be.getType())
                && !OverlayRenderer.isBreaking(be.getBlockPos().asLong(), destructionProgress)) {
            return null;
        }

        return op.call(dispatcher, be, partialTicks, overlay);
    }

    /**
     * After VulkanMod has finished its own block-entity submission loop, push our accumulated alt
     * render states through {@link AltRenderDispatcher#submit} so they actually draw, then clear
     * the per-frame buffer.
     */
    @Inject(method = "renderBlockEntities", at = @At("RETURN"), remap = false, require = 0)
    private void rbutils$bbe$submitAltStates(PoseStack poseStack,
                                             LevelRenderState levelRenderState,
                                             SubmitNodeStorage submitNodes,
                                             Long2ObjectMap<SortedSet<BlockDestructionProgress>> progression,
                                             CallbackInfo ci) {
        // Clear the flag we set at HEAD so it doesn't leak into other rendering phases.
        BBE.GlobalScope.limitVanillaSignRendering = false;

        AltRenderDispatcher dispatcher = BBE.GlobalScope.altRenderDispatcher;
        if (dispatcher == null) {
            return;
        }

        if (BBE.GlobalScope.altBlockEntityRenderStates.isEmpty()) {
            dispatcher.clearStateRendererPairs();
            return;
        }

        Vec3 camPos = levelRenderState.cameraRenderState.pos;
        double camX = camPos.x();
        double camY = camPos.y();
        double camZ = camPos.z();

        for (BlockEntityRenderState state : BBE.GlobalScope.altBlockEntityRenderStates) {
            BlockPos pos = state.blockPos;
            poseStack.pushPose();
            poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);
            try {
                dispatcher.submit(state, poseStack, submitNodes, levelRenderState.cameraRenderState);
            } catch (Throwable t) {
                BBE.getLogger().error("Alt renderer submit failed", t);
            }

            poseStack.popPose();
        }

        BBE.GlobalScope.altBlockEntityRenderStates.clear();
        dispatcher.clearStateRendererPairs();
    }
}
