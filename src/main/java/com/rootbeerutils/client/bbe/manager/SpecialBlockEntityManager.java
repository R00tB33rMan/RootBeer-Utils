/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.manager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignText;

public final class SpecialBlockEntityManager {

    private SpecialBlockEntityManager() {
    }

    public static boolean hasAnyText(SignText text, boolean filtered) {
        if (text == null) {
            return false;
        }

        Component[] lines = text.getMessages(filtered);
        for (int i = 0; i < 4; i++) {
            if (!lines[i].getString().isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
