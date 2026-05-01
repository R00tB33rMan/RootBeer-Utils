package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.quickpack.FastFilePackResources;
import com.rootbeerutils.client.quickpack.QuickPackConfig;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

@Mixin(FilePackResources.FileResourcesSupplier.class)
public abstract class FileResourcesSupplierMixin {

    @Shadow @Final private File content;

    @Inject(method = "openFull", at = @At("HEAD"), cancellable = true)
    private void rbutils$useFastFilePackResources(PackLocationInfo location,
                                                  Pack.Metadata metadata,
                                                  CallbackInfoReturnable<PackResources> cir) {
        if (!QuickPackConfig.isEnabled()) {
            return;
        }

        ZipFile zipFile;
        try {
            zipFile = new ZipFile(this.content);
        } catch (IOException e) {
            FastFilePackResources.LOGGER.error("Failed to open pack {}", this.content, e);
            return; // Vanilla fall-through
        }

        cir.setReturnValue(new FastFilePackResources(location, zipFile, metadata.overlays()));
    }
}
