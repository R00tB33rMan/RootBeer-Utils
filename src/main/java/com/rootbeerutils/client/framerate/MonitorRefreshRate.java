package com.rootbeerutils.client.framerate;

import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public final class MonitorRefreshRate {

    /**
     * Returned when GLFW can't tell us about any monitor at all.
     */
    private static final int FALLBACK_RATE = 60;

    private MonitorRefreshRate() {
    }

    /**
     * Returns the refresh rate (Hz) of the monitor currently underneath the Minecraft window.
     */
    public static int forActiveWindow() {
        long window = Minecraft.getInstance().getWindow().handle();
        long monitor = GLFW.glfwGetWindowMonitor(window);

        if (monitor == 0L) {
            monitor = findMonitorContainingWindow(window);
        }

        if (monitor == 0L) {
            monitor = GLFW.glfwGetPrimaryMonitor();
        }

        if (monitor == 0L) {
            return FALLBACK_RATE;
        }

        GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);
        if (mode == null) {
            return FALLBACK_RATE;
        }

        int rate = mode.refreshRate();
        return rate > 0 ? rate : FALLBACK_RATE;
    }

    /**
     * Walks the list of attached monitors and returns whichever one's work area contains the
     * window's center point, or {@code 0L} if no monitor contains it (e.g., window dragged
     * off-screen).
     */
    private static long findMonitorContainingWindow(long window) {
        int[] xPos = new int[1];
        int[] yPos = new int[1];
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowPos(window, xPos, yPos);
        GLFW.glfwGetWindowSize(window, width, height);
        int centreX = xPos[0] + width[0] / 2;
        int centreY = yPos[0] + height[0] / 2;

        PointerBuffer monitors = GLFW.glfwGetMonitors();
        if (monitors == null) {
            return 0L;
        }

        int[] mx = new int[1];
        int[] my = new int[1];
        int[] mw = new int[1];
        int[] mh = new int[1];
        for (int i = 0; i < monitors.limit(); i++) {
            long candidate = monitors.get(i);
            GLFW.glfwGetMonitorWorkarea(candidate, mx, my, mw, mh);
            if (centreX >= mx[0] && centreX < mx[0] + mw[0]
                && centreY >= my[0] && centreY < my[0] + mh[0]) {
                return candidate;
            }
        }

        return 0L;
    }
}
