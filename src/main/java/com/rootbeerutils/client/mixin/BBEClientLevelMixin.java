/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 *
 * On every server-verified block-state update on the client, run lid-sync so opening one half of
 * a double chest visually animates the other half (in IMMEDIATE mode) instead of leaving it
 * baked into terrain.
 */
package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.util.LidControllerSync;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class BBEClientLevelMixin {

    @Inject(method = "setServerVerifiedBlockState", at = @At("TAIL"), require = 0)
    private void rbutils$bbe$syncLidControllers(BlockPos blockPos, BlockState blockState, int i, CallbackInfo ci) {
        LidControllerSync.sync((ClientLevel) (Object) this, blockPos, blockState);
    }
}
