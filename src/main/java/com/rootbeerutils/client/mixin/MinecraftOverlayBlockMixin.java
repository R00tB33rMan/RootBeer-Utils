package com.rootbeerutils.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftOverlayBlockMixin {

    /**
     * Pretend there's no overlay for {@code handleKeybinds()}'s overlay-gated branch (the
     * later short-circuit inside the method body). No ordinal — there's only one overlay
     * read in this method, so an unanchored wrap catches it.
     */
    @WrapOperation(
        method = "handleKeybinds",
        at = @At(
              value = "FIELD",
              target = "Lnet/minecraft/client/Minecraft;overlay:Lnet/minecraft/client/gui/screens/Overlay;",
              opcode = Opcodes.GETFIELD)
    )
    private Overlay rbutils$unblockKeybindsBranch(Minecraft instance, Operation<Overlay> original) {
        Overlay actual = original.call(instance);
        return (actual instanceof LoadingOverlay) ? null : actual;
    }

    /**
     * Pretend there's no overlay for {@code tick()}'s gate before {@code handleKeybinds} is
     * invoked. This is the gate that blocks all keybind dispatches during a reload.
     * Ordinal 2 = the third overlay read in {@code tick()}, which is the gate; the first
     * two are for ticking the overlay itself.
     */
    @WrapOperation(
        method = "tick",
        at = @At(
              value = "FIELD",
              target = "Lnet/minecraft/client/Minecraft;overlay:Lnet/minecraft/client/gui/screens/Overlay;",
              ordinal = 2,
              opcode = Opcodes.GETFIELD)
    )
    private Overlay rbutils$unblockTickGate(Minecraft instance, Operation<Overlay> original) {
        Overlay actual = original.call(instance);
        return (actual instanceof LoadingOverlay) ? null : actual;
    }

    /**
     * Pretend there's no overlay for the first overlay-gated branch in {@code doWorldLoad},
     * so a fresh world load doesn't get bounced when a reload overlay is still mid-fade.
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
