/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 *
 * FRAPI rewrite of BBE's Sodium-coupled BlockRenderHelper. Emits a list of {@code BlockStateModelPart}s
 * through a FRAPI {@code QuadEmitter}, applying per-quad transforms (UV swap, rotation, color)
 * via a transform pushed onto the emitter for the duration of each part's emission.
 *
 * <p>Sprite swap: BBE's geometry registry bakes each model layer with a fixed "placeholder" sprite
 * ({@code GeometryRegistry.PlaceHolderSpriteIdentifiers}). At chunk-emit time the caller passes in
 * the {@code sourceSprite} that the geometry was baked with and the {@code targetSprite} they
 * want; we remap UVs by rescaling within the source-sprite extent and re-projecting into the
 * target-sprite extent (the same math BBE's Sodium {@code QuadTransform.swapSprite} did).</p>
 */
package com.rootbeerutils.client.bbe.pipeline;

import com.rootbeerutils.client.bbe.util.QuadTransform;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public final class BlockRenderHelper {

    private final QuadEmitter emitter;

    private @Nullable TextureAtlasSprite targetSprite;
    private @Nullable TextureAtlasSprite sourceSprite;
    private @Nullable ChunkSectionLayer rendertype;
    private float @Nullable [] rotation;
    private int color = -1;

    public BlockRenderHelper(QuadEmitter emitter) {
        this.emitter = emitter;
    }

    /**
     * Atlas-sprite swap source — the sprite the geometry was baked with.
     */
    public void setSourceSprite(@Nullable TextureAtlasSprite sprite) { this.sourceSprite = sprite; }

    /**
     * Sets the material (resolves to a sprite via the block atlas) used as the target sprite.
     */
    public void setMaterial(@Nullable SpriteId material) {
        this.targetSprite = (material != null) ? QuadTransform.getSprite(material.texture()) : null;
    }

    /**
     * Direct override — bypasses material→sprite resolution (used by copper-golem oxidation states).
     */
    public void setSprite(@Nullable TextureAtlasSprite sprite) {
        this.targetSprite = sprite;
    }

    public void setRendertype(@Nullable ChunkSectionLayer layer) {
        this.rendertype = layer;
    }

    public void setRotation(float @Nullable [] rotation) {
        this.rotation = rotation;
    }

    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Emit a list of model parts through the FRAPI emitter, applying per-quad transforms.
     * @param parts the BlockStateModelParts to emit
     * @param state the block state being meshed (used for facing-derived rotation)
     * @param cullTest the FRAPI cull predicate from the chunk pipeline
     */
    public void emitParts(List<BlockStateModelPart> parts,
                          BlockState state,
                          Predicate<@Nullable Direction> cullTest) {
        float stateRotation = getRotationFromBlockState(state);
        boolean swapSprites = sourceSprite != null && targetSprite != null && sourceSprite != targetSprite;

        emitter.pushTransform(quad -> {
            if (rendertype != null) {
                quad.chunkLayer(rendertype);
            }

            quad.ambientOcclusion(TriState.DEFAULT);

            if (swapSprites) {
                QuadTransform.swapSprite(sourceSprite, targetSprite, quad);
            }

            if (rotation != null) {
                if (rotation[0] != 0) {
                    QuadTransform.rotateX(quad, rotation[0]);
                }

                if (rotation[1] != 0) {
                    QuadTransform.rotateY(quad, rotation[1]);
                }
            } else if (stateRotation != 0f) {
                QuadTransform.rotateY(quad, stateRotation);
            }

            if (color != -1) {
                for (int v = 0; v < 4; v++) {
                    quad.color(v, color);
                }
            }

            return true;
        });

        try {
            for (BlockStateModelPart part : parts) {
                part.emitQuads(emitter, cullTest);
            }
        } finally {
            emitter.popTransform();
        }
    }

    public static float getRotationFromBlockState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return getRotationFromFacing(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }

        if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
            return compute16StepRotation(state);
        }

        return 0f;
    }

    public static float getRotationFromFacing(Direction facing) {
        return switch (facing) {
            case NORTH, DOWN -> 180f;
            case EAST  -> 270f;
            case SOUTH, UP -> 0f;
            case WEST  -> 90f;
        };
    }

    public static float compute16StepRotation(BlockState state) {
        int rot = state.getValue(BlockStateProperties.ROTATION_16);
        return rot * 22.5f;
    }
}
