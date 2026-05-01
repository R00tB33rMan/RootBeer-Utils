package com.rootbeerutils.client.asyncpackscan;

/**
 * Narrows {@link AutoCloseable} so {@code close()} doesn't declare {@code throws Exception}.
 * Used by {@link ResourcePackOrganizerLockState} to hand back a lambda guard that callers can
 * pop into a try-with-resource without forcing every caller to handle a checked exception that
 * a lock unlocked won't actually throw.
 *
 * <p>Adapted from JFronny's async-pack-scan mod.
 */
public interface SafeCloseable extends AutoCloseable {

    @Override
    void close();
}
