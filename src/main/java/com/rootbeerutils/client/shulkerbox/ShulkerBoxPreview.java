package com.rootbeerutils.client.shulkerbox;

import com.mojang.blaze3d.platform.InputConstants;
import com.rootbeerutils.client.mixin.KeyMappingAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Entrypoint that registers the shulker-preview tooltip mapping
 * ({@link ShulkerBoxPreviewTooltipComponent} → {@link ShulkerBoxPreviewClientTooltipComponent})
 * and owns the "lock + hover" key. The actual data-component → tooltip-component plumbing
 * lives in {@link com.rootbeerutils.client.mixin.ItemStackShulkerPreviewMixin}.
 */
public class ShulkerBoxPreview implements ClientModInitializer {

    private static KeyMapping lockKey;

    @Override
    public void onInitializeClient() {
        lockKey = new KeyMapping("key.rootbeerutils.shulker_lock_tooltip",
                                 GLFW.GLFW_KEY_LEFT_CONTROL,
                                 KeyMapping.Category.MISC);
        KeyMappingHelper.registerKeyMapping(lockKey);

        ClientTooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof ShulkerBoxPreviewTooltipComponent component) {
                return new ShulkerBoxPreviewClientTooltipComponent(component);
            }

            return null;
        });
    }

    /**
     * Reads the GLFW raw key state instead of {@link KeyMapping#isDown()} so the lock works
     * inside screens (where vanilla suppresses isDown for non-text keys).
     */
    public static boolean isLockKeyPressed() {
        if (lockKey == null) {
            return false;
        }

        InputConstants.Key key = ((KeyMappingAccessor) lockKey).rbutils$getBoundKey();
        if (key.getType() != InputConstants.Type.KEYSYM
            || key.getValue() == InputConstants.UNKNOWN.getValue()) {
            return false;
        }

        long handle = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(handle, key.getValue()) == GLFW.GLFW_PRESS;
    }
}
