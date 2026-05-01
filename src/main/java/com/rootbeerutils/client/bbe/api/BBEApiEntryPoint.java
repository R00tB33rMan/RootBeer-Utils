/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.api;

/**
 * Implemented by addon mods to register alt renderers. Wired via the
 * {@code bbe:renderer_registration_api} Fabric entrypoint.
 */
public interface BBEApiEntryPoint {
    void registerRenderers(AltRendererRegistration context);
}
