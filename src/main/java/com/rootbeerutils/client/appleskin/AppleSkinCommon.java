package com.rootbeerutils.client.appleskin;

import net.fabricmc.api.ModInitializer;
import com.rootbeerutils.client.appleskin.network.SyncHandler;

public class AppleSkinCommon implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        SyncHandler.init();
    }
}
