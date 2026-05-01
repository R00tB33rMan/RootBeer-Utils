/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.api;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AltRenderDispatcher implements ResourceManagerReloadListener {

    private Map<BlockEntityType<?>, List<AltRenderer<?, ?>>> renderers = ImmutableMap.of();
    private final Map<BlockEntityRenderState, AltRenderer<?, ?>> stateRendererPairs = new HashMap<>();

    private final Font font;
    private final Supplier<EntityModelSet> entityModelSet;
    private Vec3 cameraPos;
    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;
    private final EntityRenderDispatcher entityRenderer;
    private final SpriteGetter sprites;
    private final PlayerSkinRenderCache playerSkinRenderCache;

    public AltRenderDispatcher(final Font font,
                               final Supplier<EntityModelSet> entityModelSet,
                               final BlockModelResolver blockModelResolver,
                               final ItemModelResolver itemModelResolver,
                               final EntityRenderDispatcher entityRenderer,
                               final SpriteGetter sprites,
                               final PlayerSkinRenderCache playerSkinRenderCache) {
        this.blockModelResolver = blockModelResolver;
        this.itemModelResolver = itemModelResolver;
        this.entityRenderer = entityRenderer;
        this.font = font;
        this.entityModelSet = entityModelSet;
        this.sprites = sprites;
        this.playerSkinRenderCache = playerSkinRenderCache;
    }

    @SuppressWarnings("unchecked")
    public <E extends BlockEntity, S extends BlockEntityRenderState> List<AltRenderer<E, S>> getRenderers(final E blockEntity) {
        return (List<AltRenderer<E, S>>) (List<?>) this.renderers.getOrDefault(blockEntity.getType(), List.of());
    }

    public void prepare(final Vec3 cameraPos) {
        this.cameraPos = cameraPos;
    }

    /**
     * No logging in the hotpath — we just throw; an error here is unexpected and almost always a
     * caller bug rather than the implementing addon's fault.
     */
    public <E extends BlockEntity, S extends BlockEntityRenderState> List<S> tryExtractRenderStates(final E blockEntity,
                                                                                                    final @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        List<AltRenderer<E, S>> renderers = this.getRenderers(blockEntity);

        if (renderers.isEmpty()) {
            return List.of();
        }
        if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState())) {
            return List.of();
        }

        List<S> states = new ArrayList<>();

        for (AltRenderer<E, S> renderer : renderers) {
            RegistrationInfo regInfo = AltRenderers.forRenderer(renderer);
            if (regInfo == null) {
                throw new RuntimeException("RegistrationInfo for a registered AltRenderer was null!");
            }

            if (!renderer.shouldRender(blockEntity, this.cameraPos)) {
                continue;
            }

            S state = renderer.createRenderState();
            addStateRendererPair(state, renderer);
            renderer.extractRenderState(blockEntity, state, breakProgress);
            states.add(state);
        }

        return states;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <S extends BlockEntityRenderState> void submit(final S state,
                                                          final PoseStack poseStack,
                                                          final SubmitNodeCollector submitNodeCollector,
                                                          final CameraRenderState camera) {
        AltRenderer renderer = stateRendererPairs.get(state);
        if (renderer == null) {
            throw new RuntimeException("Could not map this BlockEntityRenderState to a registered AltRenderer -> " + state);
        }

        try {
            renderer.submit(state, poseStack, submitNodeCollector, camera);
        } catch (Exception e) {
            throw new RuntimeException("An exception was caught inside a registered AltRenderer", e);
        }
    }

    private <E extends BlockEntity, S extends BlockEntityRenderState> void addStateRendererPair(S state, AltRenderer<E, S> renderer) {
        stateRendererPairs.put(state, renderer);
    }

    public void clearStateRendererPairs() {
        stateRendererPairs.clear();
    }

    @Override
    public void onResourceManagerReload(final @NonNull ResourceManager resourceManager) {
        AltRendererProvider.Context context = new AltRendererProvider.Context(
                this,
                this.blockModelResolver,
                this.itemModelResolver,
                this.entityRenderer,
                this.entityModelSet.get(),
                this.font,
                this.sprites,
                this.playerSkinRenderCache
        );
        this.renderers = AltRenderers.createAltEntityRenderers(context);
    }
}
