/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.model;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.vertex.PoseStack;

import com.rootbeerutils.client.bbe.BBE;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

public final class ModelUtility {

    private ModelUtility() {
    }

    public static void toBakedQuads(ModelPart part, List<BakedQuad> output, TextureAtlasSprite sprite, PoseStack stack) {
        part.visit(stack, (pose, _, _, cube) -> {
            for (ModelPart.Polygon poly : cube.polygons) {
                if (poly.vertices().length != 4) {
                    BBE.getLogger().error("Non-quad polygon detected when assembling block geometry; skipping");
                    continue;
                }

                Vector3f[] positions = new Vector3f[4];
                long[] packedUvs = new long[4];

                Direction dir = normalToDirection(poly.normal());

                for (int i = 0; i < 4; i++) {
                    ModelPart.Vertex vertex = poly.vertices()[i];
                    Vector3f vec = pose.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
                    positions[i] = vec;

                    float u = sprite.getU(vertex.u());
                    float v = sprite.getV(vertex.v());
                    packedUvs[i] = UVPair.pack(u, v);
                }

                Material.Baked bakedMat = new Material.Baked(sprite, false);
                BakedQuad.MaterialInfo matInfo = BakedQuad.MaterialInfo.of(bakedMat, Transparency.NONE, -1, true, 0);

                BakedQuad baked = new BakedQuad(
                        positions[0], positions[1], positions[2], positions[3],
                        packedUvs[0], packedUvs[1], packedUvs[2], packedUvs[3],
                        dir,
                        matInfo
                );
                output.add(baked);
            }
        });
    }

    /**
     * Best-effort mapping of a polygon normal onto one of the six cardinal directions. Used to
     * tag baked quads with their face direction for chunk-mesh culling.
     */
    public static Direction normalToDirection(Vector3fc normal) {
        for (Direction dir : Direction.values()) {
            if (dir.getStepX() == normal.x() &&
                dir.getStepY() == normal.y() &&
                dir.getStepZ() == normal.z()) {
                return dir;
            }
        }

        float x = normal.x();
        float y = normal.y();
        float z = normal.z();
        float absX = Math.abs(x);
        float absY = Math.abs(y);
        float absZ = Math.abs(z);

        if (absX > absY && absX > absZ) {
            return x > 0 ? Direction.EAST : Direction.WEST;
        }

        if (absY > absZ) {
            return y > 0 ? Direction.UP   : Direction.DOWN;
        }

        return z > 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
