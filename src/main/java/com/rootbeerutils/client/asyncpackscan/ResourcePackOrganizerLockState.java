package com.rootbeerutils.client.asyncpackscan;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResourcePackOrganizerLockState {

    private final Lock resourcesLock = new ReentrantLock();
    private final Lock scanStateLock = new ReentrantLock();
    private boolean isRunning = false;
    private boolean isScheduled = false;

    /**
     * Acquires the resource-list lock and returns a guard that releases it on close. Designed for
     * try-with-resources usage at every read or write site of {@code selected}/{@code unselected}.
     */
    public SafeCloseable lockResources() {
        resourcesLock.lock();
        return resourcesLock::unlock;
    }

    /**
     * Records that a scan has been requested. If no scan is currently running, the caller should
     * start one immediately ({@code shouldStart=true}); otherwise we mark a follow-up as
     * scheduled and the running scan will pick it up on completion.
     */
    public RequestScanResponse requestScan() {
        scanStateLock.lock();
        try {
            if (isRunning) {
                isScheduled = true;
                return new RequestScanResponse(false);
            }

            isRunning = true;
            return new RequestScanResponse(true);
        } finally {
            scanStateLock.unlock();
        }
    }

    /**
     * Records that the running scan has finished. If a follow-up was scheduled while it ran,
     * {@code shouldContinue=true} tells the caller to immediately kick off another scan;
     * otherwise we drop back to the idle state.
     */
    public ScanFinishedResponse emitScanFinished() {
        scanStateLock.lock();
        try {
            if (isScheduled) {
                isScheduled = false;
                return new ScanFinishedResponse(true);
            }

            isRunning = false;
            return new ScanFinishedResponse(false);
        } finally {
            scanStateLock.unlock();
        }
    }

    public record RequestScanResponse(boolean shouldStart) {
    }

    public record ScanFinishedResponse(boolean shouldContinue) {
    }
}
