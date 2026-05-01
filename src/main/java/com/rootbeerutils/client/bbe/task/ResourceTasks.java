/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.task;

import com.rootbeerutils.client.bbe.BBE;
import com.rootbeerutils.client.bbe.model.GeometryRegistry;
import com.rootbeerutils.client.bbe.model.ModelGenerator;

public final class ResourceTasks {

    private ResourceTasks() {
    }

    public static final int FAILED = 0xFFFF;
    public static final int COMPLETE = 0x0000;

    public static int populateGeometryRegistry() {
        try {
            if (!GeometryRegistry.getCache().isEmpty()) {
                BBE.getLogger().info("Clearing geometry registry");
                GeometryRegistry.clearCache();
            }

            if (ModelGenerator.generateAppend() == COMPLETE) {
                BBE.getLogger().info("Geometry registry populated");
                return COMPLETE;
            } else {
                BBE.getLogger().error("Could not prepare geometry — entityModelSet was null. Check earlier logs.");
                return FAILED;
            }
        } catch (Throwable t) {
            BBE.getLogger().error("Geometry setup task failed with internal error", t);
            return FAILED;
        }
    }
}
