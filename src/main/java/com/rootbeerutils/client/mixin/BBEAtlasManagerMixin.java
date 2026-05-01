/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * BBE bakes entity textures into the block atlas (so the chunk emitter can reach them with a
 * standard atlas-sprite lookup). Vanilla iterates all sprites on each atlas update to detect
 * duplicates; we skip {@code entity/} sprites in that pass to avoid spurious duplicate warnings
 * and the associated work.
 */
@Mixin(AtlasManager.class)
public class BBEAtlasManagerMixin {

    @Redirect(method = "updateSpriteMaps",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"),
            require = 0
    )
    private void rbutils$bbe$cancelForEach(Map<SpriteId, TextureAtlasSprite> instance, BiConsumer<SpriteId, TextureAtlasSprite> consumer) {
        instance.forEach((material, sprite) -> {
            Identifier tex = material.texture();
            if (tex.getPath().startsWith("entity/")) return;
            consumer.accept(material, sprite);
        });
    }
}
