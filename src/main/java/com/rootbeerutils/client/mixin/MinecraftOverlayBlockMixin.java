package com.rootbeerutils.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftOverlayBlockMixin {

    /** Pretend there's no overlay for {@code handleKeybinds()}'s early-out check. */
    @WrapOperation(
        method = "handleKeybinds",
        at = @At(
              value = "FIELD",
              target = "Lnet/minecraft/client/Minecraft;overlay:Lnet/minecraft/client/gui/screens/Overlay;",
              opcode = Opcodes.GETFIELD)
    )
    private Overlay rbutils$unblockKeybinds(Minecraft instance, Operation<Overlay> original) {
        Overlay actual = original.call(instance);
        return (actual instanceof LoadingOverlay) ? null : actual;
    }

    /** Pretend there's no overlay for {@code tick()}'s second overlay-gated branch. */
    @WrapOperation(
        method = "tick",
        at = @At(
              value = "FIELD",
              target = "Lnet/minecraft/client/Minecraft;overlay:Lnet/minecraft/client/gui/screens/Overlay;",
              ordinal = 2,
              opcode = Opcodes.GETFIELD)
    )
    private Overlay rbutils$unblockTick(Minecraft instance, Operation<Overlay> original) {
        Overlay actual = original.call(instance);
        return (actual instanceof LoadingOverlay) ? null : actual;
    }

    /**
     * Pretend there's no overlay for the first overlay-gated branch in {@code doWorldLoad}, so
     * a fresh world load doesn't get bounced when the title-screen reload overlay is still
     * mid-fade.
     */
    @WrapOperation(
        method = "doWorldLoad",
        at = @At(
              value = "FIELD",
              target = "Lnet/minecraft/client/Minecraft;overlay:Lnet/minecraft/client/gui/screens/Overlay;",
              ordinal = 0,
              opcode = Opcodes.GETFIELD)
    )
    private Overlay rbutils$unblockWorldLoad(Minecraft instance, Operation<Overlay> original) {
        Overlay actual = original.call(instance);
        return (actual instanceof LoadingOverlay) ? null : actual;
    }
}
