package com.rootbeerutils.client.mixin;

import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private {@code connection} field of {@code ServerReconfigScreen} so the
 * bridge swap can pass it on to {@link com.rootbeerutils.client.gui.ReconfigBridgeScreen}.
 */
@Mixin(ServerReconfigScreen.class)
public interface ServerReconfigScreenAccessor {

    @Accessor("connection")
    Connection rbutils$getConnection();
}
