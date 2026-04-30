package com.rootbeerutils.client.shulkerbox;

import com.rootbeerutils.client.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Renders the shulker-box-style tooltip preview: a vanilla-skinned 3×9 grid drawn from the
 * {@code shulker_box.png} GUI texture, populated with the contained items.
 *
 * <p>While {@link ShulkerBoxPreview#isLockKeyPressed()} is true the slot under the mouse is
 * highlighted and an inner item tooltip is rendered for the hovered stack via
 * {@link #renderInnerTooltip(GuiGraphicsExtractor, Font, ItemStack, int, int, GuiGraphicsExtractorAccessor)}.
 * The mixin {@link GuiGraphicsExtractorAccessor} exposes the deferred-tooltip slot so we can
 * render the inner tooltip on a higher stratum without wiping the outer one.
 */
public class ShulkerBoxPreviewClientTooltipComponent implements ClientTooltipComponent {

    private static final Identifier TEXTURE =
        Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png");

    private static final int COLS            = 9;
    private static final int ROWS            = 3;
    private static final int SLOT_SIZE       = 18;
    private static final int WIDTH           = 176;
    private static final int H_TOP           = 7;
    private static final int H_GRID          = 54;
    private static final int H_BOTTOM        = 7;
    private static final int TEX_V_BOT       = 160;
    private static final int ITEM_LEFT       = 8;
    private static final int ITEM_TOP        = 8;
    private static final int BOTTOM_PAD      = 3;
    private static final int TEX_SIZE        = 256;
    private static final int SLOT_HIGHLIGHT  = 0x80FFFFFF; // -2130706433

    private final List<ItemStack> items;

    public ShulkerBoxPreviewClientTooltipComponent(ShulkerBoxPreviewTooltipComponent component) {
        this.items = component.items();
    }

    @Override
    public int getHeight(@NotNull Font font) {
        return H_TOP + H_GRID + H_BOTTOM + BOTTOM_PAD; // 71
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return WIDTH; // 176
    }

    @Override
    public void extractImage(@NotNull Font font, int x, int y, int totalWidth, int totalHeight,
                             @NotNull GuiGraphicsExtractor graphics) {
        // Background: top, slot grid, bottom — sliced from the shulker_box GUI texture.
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y,
                      0.0F, 0.0F, WIDTH, H_TOP, TEX_SIZE, TEX_SIZE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + H_TOP,
                      0.0F, 17.0F, WIDTH, H_GRID, TEX_SIZE, TEX_SIZE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + H_TOP + H_GRID,
                      0.0F, (float) TEX_V_BOT, WIDTH, H_BOTTOM, TEX_SIZE, TEX_SIZE);

        GuiGraphicsExtractorAccessor accessor = (GuiGraphicsExtractorAccessor) graphics;
        int mouseX = accessor.rbutils$getMouseX();
        int mouseY = accessor.rbutils$getMouseY();

        int hoveredSlot = ShulkerBoxPreview.isLockKeyPressed()
            ? slotAt(mouseX - x - ITEM_LEFT, mouseY - y - ITEM_TOP)
            : -1;

        int slotCount = Math.min(this.items.size(), COLS * ROWS);
        for (int slot = 0; slot < slotCount; slot++) {
            int ix = x + ITEM_LEFT + (slot % COLS) * SLOT_SIZE;
            int iy = y + ITEM_TOP  + (slot / COLS) * SLOT_SIZE;
            ItemStack stack = this.items.get(slot);
            if (!stack.isEmpty()) {
                graphics.item(stack, ix, iy);
                graphics.itemDecorations(font, stack, ix, iy);
            }
            if (slot == hoveredSlot) {
                graphics.fill(ix, iy, ix + 16, iy + 16, SLOT_HIGHLIGHT);
            }
        }

        if (hoveredSlot < 0) {
            return;
        }

        ItemStack hoveredStack = this.items.get(hoveredSlot);
        if (hoveredStack.isEmpty()) {
            return;
        }

        renderInnerTooltip(graphics, font, hoveredStack, mouseX, mouseY, accessor);
    }

    private int slotAt(int relX, int relY) {
        if (relX < 0 || relY < 0) {
            return -1;
        }

        int col = relX / SLOT_SIZE;
        int row = relY / SLOT_SIZE;

        if (col >= COLS || row >= ROWS) {
            return -1;
        }

        return row * COLS + col;
    }

    /**
     * Lets vanilla compute the tooltip for {@code stack} into the deferred-tooltip slot, then
     * promotes it onto a fresh stratum so it draws over the outer tooltip without erasing it.
     */
    private void renderInnerTooltip(GuiGraphicsExtractor graphics, Font font, ItemStack stack,
                                    int mouseX, int mouseY, GuiGraphicsExtractorAccessor accessor) {
        Runnable saved = accessor.rbutils$getDeferredTooltip();
        accessor.rbutils$setDeferredTooltip(null);
        graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
        Runnable inner = accessor.rbutils$getDeferredTooltip();
        if (inner != null) {
            graphics.nextStratum();
            inner.run();
        }

        accessor.rbutils$setDeferredTooltip(saved);
    }
}
