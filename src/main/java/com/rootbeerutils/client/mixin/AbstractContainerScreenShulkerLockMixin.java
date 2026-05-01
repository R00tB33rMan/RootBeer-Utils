package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.shulkerbox.ShulkerBoxPreview;
import com.rootbeerutils.client.shulkerbox.ShulkerBoxPreviewTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenShulkerLockMixin {

    @Shadow protected @Nullable Slot hoveredSlot;
    @Shadow @Final protected AbstractContainerMenu menu;

    @Unique private @Nullable Slot rbutils$lockedSlot = null;
    @Unique private int rbutils$lockedX = 0;
    @Unique private int rbutils$lockedY = 0;

    /** While locked, only the locked slot reports as "hovered" — even if the cursor isn't over it. */
    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void rbutils$forceFocusLockedSlot(Slot slot, double xm, double ym,
                                              CallbackInfoReturnable<Boolean> cir) {
        Slot locked = this.rbutils$lockedSlot;
        if (locked == null) {
            return;
        }

        // Release the lock if the slot is now empty, was removed from the menu, or the player
        // has picked something up (so they can place / shift-click without the tooltip stealing focus).
        if (!locked.hasItem()
            || !this.menu.slots.contains(locked)
            || !this.menu.getCarried().isEmpty()) {
            this.rbutils$lockedSlot = null;
            return;
        }

        cir.setReturnValue(slot == locked);
    }

    /**
     * Pin the tooltip in place while the lock key is held over a shulker preview.
     *
     * <p>The {@code Optional<TooltipComponent>} parameter mirrors vanilla's
     * {@code GuiGraphicsExtractor#setTooltipForNextFrame} signature that this {@code @Redirect}
     * is intercepting; the {@code OptionalUsedAsFieldOrParameterType} inspection doesn't apply
     * here because the type is dictated by the redirected method, not chosen by us.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Redirect(method = "extractTooltip",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"))
    private void rbutils$lockTooltipPosition(GuiGraphicsExtractor graphics, Font font,
                                             List<Component> texts, Optional<TooltipComponent> optionalImage,
                                             int xo, int yo, Identifier style) {
        boolean lockable = optionalImage.orElse(null) instanceof ShulkerBoxPreviewTooltipComponent;
        if (ShulkerBoxPreview.isLockKeyPressed() && lockable) {
            // First lock-eligible frame — snapshot tooltip origin and the slot under the cursor.
            if (this.rbutils$lockedSlot == null && this.hoveredSlot != null) {
                this.rbutils$lockedSlot = this.hoveredSlot;
                this.rbutils$lockedX    = xo;
                this.rbutils$lockedY    = yo;
            }
        } else {
            this.rbutils$lockedSlot = null;
        }

        if (this.rbutils$lockedSlot != null) {
            xo = this.rbutils$lockedX;
            yo = this.rbutils$lockedY;
        }

        graphics.setTooltipForNextFrame(font, texts, optionalImage, xo, yo, style);
    }
}
