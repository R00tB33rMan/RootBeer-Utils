/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.ext;

/**
 * Extension surface mixed onto every {@code BlockEntity}. Lets the alt-renderer pipeline mark a
 * block entity as "supported", track whether its terrain mesh has been baked, and cache its
 * optimization kind without round-tripping through a side map.
 */
public interface BlockEntityExt {

    boolean rootbeer_utils$supportedBlockEntity();
    void rootbeer_utils$supportedBlockEntity(boolean bl);

    RenderingMode rootbeer_utils$renderingMode();
    void rootbeer_utils$renderingMode(RenderingMode mode);

    boolean rootbeer_utils$terrainMeshReady();
    void rootbeer_utils$terrainMeshReady(boolean b);

    void rootbeer_utils$hasSpecialManager(boolean bl);

    byte rootbeer_utils$optKind();
    void rootbeer_utils$optKind(byte k);
}
