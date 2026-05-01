/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe.api;

import com.rootbeerutils.client.bbe.BBE;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class RegistrationCollection {

    private RegistrationCollection() {
    }

    public static final String ENTRYPOINT_KEY = "bbe:renderer_registration_api";

    private static final Map<AltRendererProvider, RegistrationInfo> REGISTRATIONS = new HashMap<>();

    public static void collectEntryPoints() {
        List<EntrypointContainer<BBEApiEntryPoint>> containers =
                FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_KEY, BBEApiEntryPoint.class);

        AltRendererRegistration context = new AltRendererRegistration();

        for (EntrypointContainer<BBEApiEntryPoint> container : containers) {
            BBEApiEntryPoint entrypoint = container.getEntrypoint();
            ModContainer provider = container.getProvider();

            String modId = provider.getMetadata().getId();
            String entryPointId = entrypoint.getClass().getName();

            try {
                entrypoint.registerRenderers(context);
            } catch (Exception e) {
                BBE.getLogger().error("Failed to register alt renderers for modId={} entrypoint={}", modId, entryPointId, e);
            }
        }
    }

    public static Map<AltRendererProvider, RegistrationInfo> getRegistrations() {
        return REGISTRATIONS;
    }
}
