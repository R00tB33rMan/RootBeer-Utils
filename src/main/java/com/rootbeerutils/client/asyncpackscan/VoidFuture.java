package com.rootbeerutils.client.asyncpackscan;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Adapter that re-types any {@link Future Future&lt;?&gt;} as {@code Future<Void>}, returning
 * {@code null} for {@code get()} regardless of the underlying task's actual result. Used so the
 * async-pack-scan API can hand callers a {@code Future<Void>} without their having to know that
 * the underlying executor returned a different type.
 *
 * <p>Adapted from JFronny's async-pack-scan mod.
 */
public record VoidFuture(Future<?> future) implements Future<Void> {

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        future.get();
        return null;
    }

    @Override
    public Void get(long timeout, @NotNull TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        future.get(timeout, unit);
        return null;
    }
}
