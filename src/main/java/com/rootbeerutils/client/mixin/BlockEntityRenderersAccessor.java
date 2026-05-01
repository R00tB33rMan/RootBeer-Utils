package com.rootbeerutils.client.mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockEntityRenderers.class)
public interface BlockEntityRenderersAccessor {

    @Invoker("register")
    static <T extends BlockEntity, S extends BlockEntityRenderState> void invokeRegister(BlockEntityType<? extends T> type,
                                                                                         BlockEntityRendererProvider<T, S> renderer) {
        throw new AssertionError();
    }
}
