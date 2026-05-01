package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.ext.BlockEntityExt;
import com.rootbeerutils.client.bbe.manager.InstancedBlockEntityManager;
import com.rootbeerutils.client.bbe.ext.RenderingMode;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CopperGolemStatueBlockEntity.class)
public class BBECopperGolemStatueBlockEntityMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        BlockEntityExt ext = (BlockEntityExt) this;

        ext.rootbeer_utils$supportedBlockEntity(true);
        ext.rootbeer_utils$renderingMode(RenderingMode.TERRAIN);
        ext.rootbeer_utils$terrainMeshReady(true);
        ext.rootbeer_utils$optKind(InstancedBlockEntityManager.OptKind.CGS);
    }
}
