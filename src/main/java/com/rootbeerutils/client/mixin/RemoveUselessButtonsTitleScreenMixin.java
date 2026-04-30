package com.rootbeerutils.client.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Strips the "Credits & Attribution" footer button after the title screen finishes its init pass.
 */
@Mixin(TitleScreen.class)
public class RemoveUselessButtonsTitleScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void rbutils$stripTitleCreditsFooter(CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        ScreenRemoveInvoker invoker = (ScreenRemoveInvoker) screen;
        List<GuiEventListener> toRemove = new ArrayList<>();
        for (GuiEventListener guiEventListener : screen.children()) {
            if (guiEventListener instanceof AbstractWidget abstractWidget) {
                Component message = abstractWidget.getMessage();
                if (Component.translatable("title.credits").equals(message)) {
                    toRemove.add(guiEventListener);
                }
            }
        }

        for (GuiEventListener guiEventListener : toRemove) {
            invoker.rbutils$invokeRemove(guiEventListener);
        }
    }
}
