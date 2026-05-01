package com.rootbeerutils.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import com.rootbeerutils.client.asyncpackscan.AsyncResourcePackManager;
import com.rootbeerutils.client.asyncpackscan.ResourcePackOrganizerLockState;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
@Mixin(PackSelectionModel.class)
public abstract class PackSelectionModelAsyncMixin {

    @Unique
    private static final Logger RBUTILS$LOGGER = LogUtils.getLogger();

    @Shadow @Final private PackRepository repository;
    @Shadow @Final private List<Pack> selected;
    @Shadow @Final private List<Pack> unselected;

    @Unique private Future<Void> rbutils$packScan = null;
    @Unique private final ResourcePackOrganizerLockState rbutils$lock = new ResourcePackOrganizerLockState();

    /**
     * Replaces vanilla's synchronous {@code findNewPacks} body with an async kick-off. The
     * actual list rebuild happens in {@link #rbutils$afterScan} once the worker pool has
     * finished the reload.
     */
    @Inject(method = "findNewPacks", at = @At("HEAD"), cancellable = true)
    private void rbutils$findNewPacksAsync(CallbackInfo ci) {
        ci.cancel();
        if (rbutils$lock.requestScan().shouldStart()) {
            rbutils$startScan();
        }
    }

    /**
     * Submits the async reload. The task reference is parked in an {@link AtomicReference} so
     * the post-reload callback can identify its own future without a forward reference to a
     * not-yet-assigned local — and without the unchecked-cast warning that a single-element
     * generic array would trigger.
     */
    @Unique
    private void rbutils$startScan() {
        AtomicReference<Future<Void>> taskRef = new AtomicReference<>();
        Future<Void> task = ((AsyncResourcePackManager) this.repository)
            .rbutils$scanPacksAsync(() -> rbutils$afterScan(taskRef.get()));
        taskRef.set(task);
        this.rbutils$packScan = task;
    }

    /**
     * Runs on the worker thread once the reload completes: rebuilds the
     * {@code selected} / {@code unselected} lists, drops Fabric mod-internal packs that asked
     * to be hidden, and triggers any follow-up scan that was requested mid-flight.
     */
    @Unique
    private void rbutils$afterScan(Future<Void> task) {
        try (var _ = rbutils$lock.lockResources()) {
            if (task.isCancelled()) return;
            this.selected.retainAll(this.repository.getAvailablePacks());
            this.unselected.clear();
            this.unselected.addAll(this.repository.getAvailablePacks());
            this.unselected.removeAll(this.selected);
            this.selected.removeIf(p -> p instanceof FabricPack fp && fp.fabric$isHidden());
            this.unselected.removeIf(p -> p instanceof FabricPack fp && fp.fabric$isHidden());
            if (this.rbutils$packScan == task) this.rbutils$packScan = null;
        }
        if (rbutils$lock.emitScanFinished().shouldContinue()) {
            rbutils$startScan();
        }
    }

    /**
     * Wraps vanilla's repository-publish so we block on any in-flight scan first, then take
     * the resource lock around vanilla's body. Without this guard, a click on "Done" while a
     * scan is still running could ship a half-scan view of the selection to the repository.
     */
    @WrapMethod(method = "updateRepoSelectedList")
    private void rbutils$lockedUpdate(Operation<Void> original) {
        try {
            if (rbutils$packScan != null) rbutils$packScan.get();
            try (var _ = rbutils$lock.lockResources()) {
                original.call();
            }
        } catch (InterruptedException | ExecutionException e) {
            RBUTILS$LOGGER.error("Async pack scan was interrupted while publishing selection", e);
        }
    }

    /**
     * Materializes vanilla's selected stream under the resource lock so the caller iterates a
     * consistent snapshot rather than a partially mutated list. Calling {@code .toList()}
     * inside the locked region is what makes the snapshot eager — the returned outer
     * {@code .stream()} can be consumed safely after the lock is released.
     */
    @WrapMethod(method = "getSelected")
    private Stream<PackSelectionModel.Entry> rbutils$lockedGetSelected(Operation<Stream<PackSelectionModel.Entry>> original) {
        try (var _ = rbutils$lock.lockResources()) {
            return original.call().toList().stream();
        }
    }

    /**
     * Same treatment as {@link #rbutils$lockedGetSelected} for the unselected list.
     */
    @WrapMethod(method = "getUnselected")
    private Stream<PackSelectionModel.Entry> rbutils$lockedGetUnselected(Operation<Stream<PackSelectionModel.Entry>> original) {
        try (var _ = rbutils$lock.lockResources()) {
            return original.call().toList().stream();
        }
    }
}
