package com.rootbeerutils.client.shulkerbox;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Data-side tooltip component carrying the contained items of a shulker box.
 * Paired with {@link ShulkerBoxPreviewClientTooltipComponent} on the client renderer side.
 */
public record ShulkerBoxPreviewTooltipComponent(List<ItemStack> items) implements TooltipComponent {
}
