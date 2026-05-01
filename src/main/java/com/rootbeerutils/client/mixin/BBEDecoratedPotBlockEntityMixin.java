package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.config.ConfigCache;
import com.rootbeerutils.client.bbe.ext.BlockEntityExt;
import com.rootbeerutils.client.bbe.manager.InstancedBlockEntityManager;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotBlockEntity.class)
public class BBEDecoratedPotBlockEntityMixin {
    @Unique private InstancedBlockEntityManager manager = new InstancedBlockEntityManager((BlockEntity)(Object)this);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        BlockEntityExt ext = (BlockEntityExt) this;
        ext.rootbeer_utils$supportedBlockEntity(true);
        ext.rootbeer_utils$optKind(InstancedBlockEntityManager.OptKind.POT);
    }

    @Inject(method = "triggerEvent", at = @At("TAIL"))
    private void onBlockEvent(int event, int data, CallbackInfoReturnable<Boolean> cir) {
        DecoratedPotBlockEntity decoratedPotBlockEntity = (DecoratedPotBlockEntity)(Object)this;
        if (decoratedPotBlockEntity.lastWobbleStyle != null) {
            manager.trigger(decoratedPotBlockEntity.wobbleStartedAtTick, decoratedPotBlockEntity.lastWobbleStyle.duration, ConfigCache.potAnims);
        }
    }
}
