/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.model;

import com.mojang.blaze3d.vertex.PoseStack;

import com.rootbeerutils.client.bbe.BBE;
import com.rootbeerutils.client.mixin.ModelPartAccessor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiPartBlockModel implements BlockStateModel {

    private final List<BlockStateModel> models = new ArrayList<>();
    private final Map<String, BlockStateModel> pairs = new HashMap<>();
    private final Material.Baked particleMaterial;

    public MultiPartBlockModel(ModelPart root, TextureAtlasSprite sprite, PoseStack stack) {
        this.particleMaterial = new Material.Baked(sprite, false);
        generateMeshModel(root, sprite, stack);
    }

    private void generateMeshModel(ModelPart root, TextureAtlasSprite sprite, PoseStack stack) {
        ModelPartAccessor modelAcc = (ModelPartAccessor) (Object) root;
        if (modelAcc == null) {
            BBE.getLogger().error("Failed to invoke accessor on root model part with sprite {}", sprite.contents().name());
            return;
        }

        Map<String, ModelPart> children = modelAcc.getChildren();
        if (children.isEmpty()) {
            BBE.getLogger().error("Root model part with sprite {} has no children, skipping", sprite.contents().name());
            return;
        }

        children.forEach((key, part) -> {
            List<BakedQuad> quads = new ArrayList<>();
            ModelUtility.toBakedQuads(part, quads, sprite, stack);
            QuadCollection collection = toUnculledCollection(quads);
            SimpleModelWrapper wrapper = new SimpleModelWrapper(collection, true, null);
            constructSingleVariants(List.of(wrapper));
            createModelPairs(key);
        });
    }

    private QuadCollection toUnculledCollection(List<BakedQuad> quads) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addUnculledFace(quad);
        }

        return builder.build();
    }

    private void constructSingleVariants(List<BlockStateModelPart> parts) {
        for (BlockStateModelPart variant : parts) {
            models.add(new SingleVariant(variant));
        }
    }

    private void createModelPairs(String key) {
        pairs.put(key, models.getLast());
    }

    public Map<String, BlockStateModel> getPairs() {
        return pairs;
    }

    @Override
    public void collectParts(@NonNull RandomSource randomSource, @NonNull List<BlockStateModelPart> list) {
        if (models.isEmpty()) {
            return;
        }

        long seed = randomSource.nextLong();
        for (BlockStateModel model : this.models) {
            randomSource.setSeed(seed);
            model.collectParts(randomSource, list);
        }
    }

    @Override
    public Material.@NonNull Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return 0;
    }
}
