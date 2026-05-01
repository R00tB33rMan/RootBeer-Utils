package com.rootbeerutils.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class CapturedFrame {

    /**
     * Texture-manager handle the captured frame is registered under.
     */
    public static final Identifier CAPTURED_FRAME_ID =
        Identifier.fromNamespaceAndPath("rootbeerutils", "captured_frame");

    /**
     * GPU debug label used when allocating the dynamic texture. Doubles as the identifier shown
     * in graphics debuggers (RenderDoc, NSight, etc.), so the captured frame is easy to spot.
     */
    private static final String TEXTURE_LABEL = "rbutils_captured_frame";

    private CapturedFrame() {
    }

    /**
     * Releases any previously captured frame texture. Safe to call when nothing is registered —
     * the texture manager treats unknown ids as a no-op.
     */
    public static void clearCapturedTexture() {
        Minecraft.getInstance().getTextureManager().release(CAPTURED_FRAME_ID);
    }

    /**
     * Asynchronously grabs the current contents of the main render target and registers the
     * resulting image under {@link #CAPTURED_FRAME_ID}. The callback fires on the render thread,
     * so registration is thread-safe.
     */
    public static void captureLastFrame() {
        final Minecraft client = Minecraft.getInstance();
        Screenshot.takeScreenshot(client.getMainRenderTarget(), nativeImage ->
            client.getTextureManager().register(
                CAPTURED_FRAME_ID,
                new DynamicTexture(() -> TEXTURE_LABEL, nativeImage)
            )
        );
    }
}
