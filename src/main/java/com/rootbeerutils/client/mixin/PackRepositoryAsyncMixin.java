package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.asyncpackscan.AsyncResourcePackManager;
import com.rootbeerutils.client.asyncpackscan.VoidFuture;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

@Mixin(PackRepository.class)
public abstract class PackRepositoryAsyncMixin implements AsyncResourcePackManager {

    @Unique
    private static final ForkJoinPool RBUTILS$POOL = ForkJoinPool.commonPool();

    @Shadow public abstract void reload();

    @Override
    public Future<Void> rbutils$scanPacksAsync(Runnable callback) {
        return new VoidFuture(RBUTILS$POOL.submit(() -> {
            reload();
            callback.run();
        }));
    }
}
