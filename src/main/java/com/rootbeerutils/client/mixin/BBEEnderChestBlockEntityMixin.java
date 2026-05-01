package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.config.ConfigCache;
import com.rootbeerutils.client.bbe.ext.BlockEntityExt;
import com.rootbeerutils.client.bbe.manager.InstancedBlockEntityManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DataFlowIssue")
@Mixin(EnderChestBlockEntity.class)
public abstract class BBEEnderChestBlockEntityMixin {
    @Unique
    private InstancedBlockEntityManager manager = new InstancedBlockEntityManager((BlockEntity)(Object)this);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        BlockEntityExt ext = (BlockEntityExt) this;
        ext.rootbeer_utils$supportedBlockEntity(true);
        ext.rootbeer_utils$optKind(InstancedBlockEntityManager.OptKind.CHEST);
    }

    @Inject(method = "lidAnimateTick", at = @At("TAIL"))
    private static void onTick(Level level, BlockPos blockPos, BlockState blockState, EnderChestBlockEntity enderChestBlockEntity, CallbackInfo ci) {
        BBEEnderChestBlockEntityMixin self = (BBEEnderChestBlockEntityMixin)(Object) enderChestBlockEntity;

        self.manager.tick(enderChestBlockEntity.getOpenNess(0.5f) > 0.01f, ConfigCache.chestAnims);
    }
}
