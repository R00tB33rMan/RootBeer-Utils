package com.rootbeerutils.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftSkipBootScreenMixin {

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/client/gui/screens/GenericMessageScreen;"
        )
    )
    private GenericMessageScreen rbutils$skipBootMessageScreen(Component message, Operation<GenericMessageScreen> original) {
        return null;
    }
}
