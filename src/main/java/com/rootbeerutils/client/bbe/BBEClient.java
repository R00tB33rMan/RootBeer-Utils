/*
 * Derived from BetterBlockEntities (LGPL-3.0). See BBE.java for details.
 */
package com.rootbeerutils.client.bbe;

import com.rootbeerutils.client.bbe.config.BBEGameOptions;
import com.rootbeerutils.client.bbe.config.BBEMenu;
import com.rootbeerutils.client.bbe.task.ManagerTasks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public final class BBEClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        File configFile = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("rootbeer-utils-bbe-options.json")
                .toFile();
        BBE.GlobalScope.options = BBEGameOptions.load(configFile);

        BBE.onClientInit();
        BBEMenu.registerIfPresent(BBE.GlobalScope.options);

        // Drain the BE manager work queue once per client tick so animation transitions get
        // serviced (chunk rebuilds queued, fences fired) at a steady cadence.
        ClientTickEvents.END_CLIENT_TICK.register(_ -> ManagerTasks.process());
    }
}
