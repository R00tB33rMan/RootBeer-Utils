/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 *
 * Rewritten for FRAPI {@code QuadEmitter} / VulkanMod's {@code MutableQuadViewImpl}. The Sodium
 * version of this class poked at quads via Sodium-specific accessors (getX/getTexU/cachedSprite);
 * the FRAPI surface uses {@code x()/y()/z()/u()/v()/pos()/uv()} and has no per-quad sprite cache,
 * so sprite swaps need both old and new sprites passed in explicitly.
 */
package com.rootbeerutils.client.bbe.util;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

public final class QuadTransform {

    private QuadTransform() {
    }

    public static void rotateY(MutableQuadView quad, float degrees) {
        if (degrees == 0f) {
            return;
        }

        float radians = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float centerX = 0.5f;
        float centerZ = 0.5f;

        for (int i = 0; i < 4; i++) {
            float x = quad.x(i) - centerX;
            float y = quad.y(i);
            float z = quad.z(i) - centerZ;
            float newX = x * cos - z * sin;
            float newZ = x * sin + z * cos;
            quad.pos(i, newX + centerX, y, newZ + centerZ);
        }
    }

    public static void rotateX(MutableQuadView quad, float degrees) {
        if (degrees == 0f) {
            return;
        }

        float radians = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float centerY = 0.5f;
        float centerZ = 0.5f;

        for (int i = 0; i < 4; i++) {
            float x = quad.x(i);
            float y = quad.y(i) - centerY;
            float z = quad.z(i) - centerZ;
            float newY = y * cos - z * sin;
            float newZ = y * sin + z * cos;
            quad.pos(i, x, newY + centerY, newZ + centerZ);
        }
    }

    /**
     * Remaps UVs from {@code oldSprite}'s atlas region into {@code newSprite}'s atlas region. Use
     * after {@code emitter.fromBakedQuad(q)} where {@code q.sprite()} is the original sprite.
     */
    public static void swapSprite(TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite, MutableQuadView quad) {
        if (oldSprite == null || newSprite == null) {
            return;
        }

        float uOldMin = oldSprite.getU0();
        float uOldMax = oldSprite.getU1();
        float vOldMin = oldSprite.getV0();
        float vOldMax = oldSprite.getV1();

        float uNewMin = newSprite.getU0();
        float uNewMax = newSprite.getU1();
        float vNewMin = newSprite.getV0();
        float vNewMax = newSprite.getV1();

        float uOldRange = uOldMax - uOldMin;
        float vOldRange = vOldMax - vOldMin;
        float uNewRange = uNewMax - uNewMin;
        float vNewRange = vNewMax - vNewMin;

        if (uOldRange == 0f || vOldRange == 0f) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            float uNorm = (quad.u(i) - uOldMin) / uOldRange;
            float vNorm = (quad.v(i) - vOldMin) / vOldRange;
            quad.uv(i, uNewMin + uNorm * uNewRange, vNewMin + vNorm * vNewRange);
        }
    }

    public static TextureAtlasSprite getSprite(Identifier id) {
        var atlas = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS);
        return atlas.getSprite(id);
    }
}
