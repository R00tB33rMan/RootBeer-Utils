package com.rootbeerutils.client.zoom;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.lwjgl.glfw.GLFW;

/**
 * Hold-to-zoom: clamps render FOV to {@link #ZOOM_HOLD_FOV} while the bound key is down,
 * forces smooth-camera while held, and restores the previous smooth-camera setting on release.
 *
 * <p>The actual FOV change happens in {@code ZoomGameRendererMixin}; the smooth-camera handover
 * happens here (so we can save/restore around the whole hold span). The camera mixin reads
 * {@code smoothCameraAtTickStart} so the value used during one tick is stable across partial-tick
 * camera updates.
 */
public class ZoomClient implements ClientModInitializer {

    public static final float ZOOM_HOLD_FOV = 25.5f;

    public static KeyMapping zoomKey;

    /**
     * Snapshot of {@link Options#smoothCamera} taken at the start of every client tick.
     */
    public static boolean smoothCameraAtTickStart;

    /**
     * True if Zoom was held last tick (used to detect press/release edges).
     */
    public static boolean zoomHeldLastTick;

    /**
     * Saved smooth-camera value to restore on release.
     */
    public static boolean savedSmoothCamera;

    @Override
    public void onInitializeClient() {
        zoomKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.rootbeerutils.zoom", GLFW.GLFW_KEY_C, KeyMapping.Category.MISC));

        ClientTickEvents.START_CLIENT_TICK.register(c ->
            smoothCameraAtTickStart = c.options.smoothCamera);

        ClientTickEvents.END_CLIENT_TICK.register(ZoomClient::onEndClientTick);
    }

    public static boolean isZoomActive() {
        Minecraft client = Minecraft.getInstance();
        if (zoomKey == null) {
            return false;
        }

        return zoomKey.isDown() && client.level != null && client.player != null;
    }

    private static void onEndClientTick(Minecraft client) {
        if (zoomKey == null) {
            return;
        }

        Options options = client.options;
        boolean held = isZoomActive();
        if (held) {
            if (!zoomHeldLastTick) {
                savedSmoothCamera = smoothCameraAtTickStart;
            }

            options.smoothCamera = true;
            zoomHeldLastTick = true;
        } else if (zoomHeldLastTick) {
            options.smoothCamera = savedSmoothCamera;
            zoomHeldLastTick = false;
        }
    }
}
