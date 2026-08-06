package com.rootbeerutils.client.appleskin;

import com.rootbeerutils.main.appleskin.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.rootbeerutils.main.appleskin.api.AppleSkinApi;
import com.rootbeerutils.main.appleskin.network.ClientSyncHandler;

public class AppleSkin implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient()
    {
        ClientSyncHandler.init();
        ModConfig.init();
        HUDOverlayHandler.init();
        TooltipOverlayHandler.init();
        FabricLoader.getInstance().getEntrypointContainers("appleskin", AppleSkinApi.class).forEach(entrypoint -> {
            try
            {
                LOGGER.info("appleskin loaded entrypoint!");
                entrypoint.getEntrypoint().registerEvents();
            }
            catch (Throwable e)
            {
                LOGGER.error("Failed to load entrypoint for mod {}", entrypoint.getProvider().getMetadata().getId(), e);
            }
        });
        DebugScreenEntries.register(DebugInfoHudEntry.ENTRY_ID, new DebugInfoHudEntry());
    }
}
