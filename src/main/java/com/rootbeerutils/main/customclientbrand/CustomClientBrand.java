package com.rootbeerutils.main.customclientbrand;

import com.rootbeerutils.main.customclientbrand.config.CCBConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomClientBrand implements ModInitializer {

    public static final String MOD_ID = "ClientBrand";
    private static final Logger LOGGER = LoggerFactory.getLogger("ClientBrand-RBU");
    public static Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(CCBConfig.class, GsonConfigSerializer::new);
        LOGGER.info(MOD_ID + " Loaded!");
    }

}
