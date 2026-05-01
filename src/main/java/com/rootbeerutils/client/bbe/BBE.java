/*
 * This file is derived from BetterBlockEntities by ceeden, licensed under LGPL-3.0.
 * Original source: https://github.com/ceeden/betterblockentities
 *
 * Modifications for VulkanMod / RootBeer-Utils integration are also distributed under LGPL-3.0.
 */
package com.rootbeerutils.client.bbe;

import com.rootbeerutils.client.bbe.api.AltRenderDispatcher;
import com.rootbeerutils.client.bbe.api.RegistrationCollection;
import com.rootbeerutils.client.bbe.config.BBEGameOptions;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class BBE {

    private BBE() {
    }

    public static final String MOD_ID = "rootbeer_utils";

    private static final Logger LOGGER = LoggerFactory.getLogger("BBE-RBU");

    public static Logger getLogger() {
        return LOGGER;
    }

    public static final class GlobalScope {

        private GlobalScope() {
        }

        /**
         * User-facing config (JSON-backed; see {@link BBEGameOptions}).
         */
        public static volatile BBEGameOptions options = new BBEGameOptions();

        /**
         * Per-frame culling frustum captured by the world renderer mixin.
         */
        public static Frustum frustum;

        /**
         * Per-frame flag set by the LevelRenderer mixin while the dedicated sign-text submit
         * pass is iterating BBE-managed signs. Tells the BBE sign renderer to skip emitting the
         * sign body (since it's already in the chunk mesh) but still render text.
         */
        public static volatile boolean limitVanillaSignRendering = false;

        /**
         * Lazily created during the first resource reload.
         */
        public static AltRenderDispatcher altRenderDispatcher;

        /**
         * Per-frame buffer of render states produced by registered alt renderers; consumed by
         * the level renderer to submit them alongside vanilla block-entity render states.
         */
        public static final List<BlockEntityRenderState> altBlockEntityRenderStates = new ArrayList<>();
    }

    /**
     * Initial registration pass. Called from the mod's client entrypoint at startup.
     */
    public static void onClientInit() {
        getLogger().info("Collecting BBE alt-renderer entrypoints...");
        RegistrationCollection.collectEntryPoints();
    }
}
