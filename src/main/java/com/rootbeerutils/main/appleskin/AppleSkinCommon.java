package com.rootbeerutils.main.appleskin;

import net.fabricmc.api.ModInitializer;
import com.rootbeerutils.main.appleskin.network.SyncHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppleSkinCommon implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize()
    {
        LOGGER.info("AppleSkinCommon (main) Loaded!");
        SyncHandler.init();
    }
}
