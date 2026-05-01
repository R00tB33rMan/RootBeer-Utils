/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.section;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SectionRebuildCallbacks {

    private SectionRebuildCallbacks() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("BBE-SectionRebuild");
    private static final ConcurrentHashMap<Long, ConcurrentLinkedQueue<Runnable>> WAITING = new ConcurrentHashMap<>();

    public static long keyFromBlockPos(BlockPos pos) {
        int sectionX = pos.getX() >> 4;
        int sectionY = pos.getY() >> 4;
        int sectionZ = pos.getZ() >> 4;
        return keyFromSectionPos(sectionX, sectionY, sectionZ);
    }

    public static long keyFromSectionPos(int sectionX, int sectionY, int sectionZ) {
        return (((long) sectionX & 0x3FFFFF) << 42)
                | (((long) sectionY & 0xFFFFF)  << 22)
                |  ((long) sectionZ & 0x3FFFFF);
    }

    public static void await(BlockPos pos, Runnable runnable) {
        long key = keyFromBlockPos(pos);
        WAITING.computeIfAbsent(key, _ -> new ConcurrentLinkedQueue<>()).add(runnable);
    }

    public static void remove(BlockPos pos) {
        long key = keyFromBlockPos(pos);
        WAITING.remove(key);
    }

    public static boolean isEmpty() {
        return WAITING.isEmpty();
    }

    /** Drain everything waiting on this section key and run it on the main thread. */
    public static void fireIfWaiting(long key) {
        ConcurrentLinkedQueue<Runnable> queue = WAITING.remove(key);
        if (queue == null) {
            return;
        }

        ArrayList<Runnable> work = new ArrayList<>();
        for (Runnable runnable; (runnable = queue.poll()) != null; ) {
            work.add(runnable);
        }

        Minecraft.getInstance().execute(() -> {
            for (Runnable runnable : work) {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    LOGGER.error("Section-rebuild fence callback failed", t);
                }
            }
        });
    }
}
