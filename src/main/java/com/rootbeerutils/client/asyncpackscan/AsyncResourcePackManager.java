package com.rootbeerutils.client.asyncpackscan;

import java.util.concurrent.Future;

/**
 * Mixed onto {@link net.minecraft.server.packs.repository.PackRepository} (see
 * {@code PackRepositoryAsyncMixin}) to expose an asynchronous variant of {@code reload()}.
 * Lets the resource-pack selection screen kick off a scan on a worker thread instead of blocking
 * the render thread for hundreds of milliseconds while it spat's every pack file.
 *
 * <p>Adapted from JFronny's async-pack-scan mod.
 */
public interface AsyncResourcePackManager {

    /**
     * Schedules the underlying {@code PackRepository.reload()} on a worker pool and returns a
     * {@link Future} that completes once both the reload and the supplied {@code callback} have
     * finished. The default body throws — if it ever runs, the mixin failed to apply.
     */
    default Future<Void> rbutils$scanPacksAsync(Runnable callback) {
        throw new IllegalStateException("Async pack scan mixin not applied to PackRepository");
    }
}
