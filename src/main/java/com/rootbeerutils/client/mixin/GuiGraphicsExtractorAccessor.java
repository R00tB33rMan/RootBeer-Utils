package com.rootbeerutils.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {

    @Accessor("mouseX")
    int rbutils$getMouseX();

    @Accessor("mouseY")
    int rbutils$getMouseY();

    @Accessor("deferredTooltip")
    @Nullable Runnable rbutils$getDeferredTooltip();

    @Accessor("deferredTooltip")
    void rbutils$setDeferredTooltip(@Nullable Runnable tooltip);
}
