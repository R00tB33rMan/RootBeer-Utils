/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.mixin;

import net.minecraft.world.level.block.entity.ChestLidController;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestLidController.class)
public interface ChestLidControllerAccessor {

    @Accessor("shouldBeOpen")
    boolean getOpen();

    @Accessor("shouldBeOpen")
    void setOpen(boolean open);

    @Accessor("openness")
    float getProgress();

    @Accessor("openness")
    void setProgress(float progress);

    @Accessor("oOpenness")
    float getLastProgress();

    @Accessor("oOpenness")
    void setLastProgress(float lastProgress);
}
