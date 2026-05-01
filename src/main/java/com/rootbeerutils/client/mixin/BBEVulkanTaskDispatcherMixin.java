/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 *
 * Pseudo mixin against VulkanMod's chunk-build task dispatcher. Equivalent of BBE's Sodium
 * RenderSectionManagerMixin: fires after a chunk section's mesh has been processed, allowing
 * code that scheduled work behind a section-rebuild fence to proceed.
 */
package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.section.SectionRebuildCallbacks;

import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.build.task.CompileResult;

import net.vulkanmod.render.chunk.build.task.TaskDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = TaskDispatcher.class, remap = false)
public class BBEVulkanTaskDispatcherMixin {

    /**
     * After {@code doSectionUpdate} commits a {@code CompileResult} (uploads its layers to the
     * GPU buffers), drain any rebuild fences waiting on that section.
     *
     * <p>{@code RETURN} fires at every return path — including the early "orphaned section"
     * return — but firing fences in the early-return case is also correct: the section won't
     * ever upload, so any waiter would be stuck forever otherwise.</p>
     *
     * <p>{@code xOffset()/yOffset()/zOffset()} return block-aligned section-origin coords
     * (multiples of 16); {@code >>4} yields the chunk-section coords matching the keys produced
     * by {@link SectionRebuildCallbacks#keyFromBlockPos}.</p>
     */
    @Inject(method = "doSectionUpdate", at = @At("RETURN"), remap = false, require = 0)
    private void rbutils$bbe$fireRebuildFences(CompileResult result, CallbackInfo ci) {
        if (SectionRebuildCallbacks.isEmpty()) {
            return;
        }

        RenderSection section = result.renderSection;
        long key = SectionRebuildCallbacks.keyFromSectionPos(
                section.xOffset() >> 4,
                section.yOffset() >> 4,
                section.zOffset() >> 4
        );
        SectionRebuildCallbacks.fireIfWaiting(key);
    }
}
