/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.task.ResourceTasks;
import com.rootbeerutils.client.bbe.task.TaskScheduler;

import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * After each successful resource reload, schedule {@link ResourceTasks#populateGeometryRegistry}
 * onto the main thread (model baking races with the render thread; main-thread is the safe path).
 */
@Mixin(ReloadableResourceManager.class)
public abstract class BBEReloadableResourceManagerMixin {

    @Inject(method = "createReload", at = @At("RETURN"))
    private void rbutils$bbe$schedulePostReloadTasks(CallbackInfoReturnable<ReloadInstance> cir) {
        ReloadInstance reload = cir.getReturnValue();
        TaskScheduler.scheduleOnReload(reload, () -> {
            if (ResourceTasks.populateGeometryRegistry() == ResourceTasks.FAILED) {
                com.rootbeerutils.client.bbe.BBE.getLogger()
                        .error("Geometry registry failed to populate after resource reload");
            }
        });
    }
}
