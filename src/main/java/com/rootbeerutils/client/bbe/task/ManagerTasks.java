/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.task;

import com.rootbeerutils.client.bbe.manager.InstancedBlockEntityManager;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class ManagerTasks {

    private ManagerTasks() {
    }

    public static final ConcurrentLinkedQueue<InstancedBlockEntityManager> WORK_QUEUE = new ConcurrentLinkedQueue<>();

    public static final int FINISHED = 0;
    public static final int PROCESSING = 1;

    public static void schedule(InstancedBlockEntityManager mgr) {
        if (mgr.tryMarkQueued()) {
            WORK_QUEUE.add(mgr);
        }
    }

    public static void process() {
        int budget = 256;
        while (budget-- > 0) {
            InstancedBlockEntityManager mgr = WORK_QUEUE.poll();
            if (mgr == null) {
                break;
            }

            mgr.clearQueued();

            int state = mgr.run();
            if (state == PROCESSING) {
                if (mgr.isValid()) {
                    schedule(mgr);
                } else {
                    mgr.forceKill();
                }
            }
        }
    }
}
