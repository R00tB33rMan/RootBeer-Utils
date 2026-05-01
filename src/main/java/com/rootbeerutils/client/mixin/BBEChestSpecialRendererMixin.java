package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.bbe.config.ConfigCache;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("DataFlowIssue")
@Mixin(ChestSpecialRenderer.class)
public class BBEChestSpecialRendererMixin {
    @Shadow @Final public static MultiblockChestResources<Identifier> CHRISTMAS;

    @Redirect(method = "submit", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/SubmitNodeCollector.submitModel (Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    public <S> void submit(SubmitNodeCollector collector, Model<S> model, S state, PoseStack poseStack, int light, int overlayCoords, int tint, SpriteId spriteId, SpriteGetter spriteGetter, int i4, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        Identifier orgContentsName = spriteId.texture();
        String path = orgContentsName.getPath();
        boolean wantChristmas = ConfigCache.christmasChests;

        if (path.contains("normal") || path.contains("trapped")) {
            // Vanilla picked regular/trapped. Force Christmas if the user wants it.
            if (wantChristmas) {
                SpriteId christmasId = Sheets.CHEST_MAPPER.apply(CHRISTMAS.single());
                collector.submitModel(model, state, poseStack, light, overlayCoords, -1, christmasId, spriteGetter, i4, null);
                return;
            }
        } else if (path.contains("christmas") || spriteId.texture().equals(CHRISTMAS.single())) {
            // Vanilla picked Christmas (this happens via the chest item's SelectItemModel even
            // outside December in some configurations). Override to regular when the user wants it.
            if (!wantChristmas) {
                SpriteId regularId = Sheets.CHEST_MAPPER.apply(ChestSpecialRenderer.REGULAR.single());
                collector.submitModel(model, state, poseStack, light, overlayCoords, -1, regularId, spriteGetter, i4, null);
                return;
            }
        }

        collector.submitModel(model, state, poseStack, light, overlayCoords, -1, spriteId, spriteGetter, i4, null);
    }
}
