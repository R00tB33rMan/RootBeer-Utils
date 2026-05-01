package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.ext.BlockEntityExt;

import com.rootbeerutils.client.bbe.manager.InstancedBlockEntityManager;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntity.class)
public class BBESignBlockEntityMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        BlockEntityExt ext = (BlockEntityExt) this;
        ext.rootbeer_utils$supportedBlockEntity(true);
        ext.rootbeer_utils$terrainMeshReady(true);
        ext.rootbeer_utils$hasSpecialManager(true);
        ext.rootbeer_utils$optKind(InstancedBlockEntityManager.OptKind.SIGN);
    }
}
