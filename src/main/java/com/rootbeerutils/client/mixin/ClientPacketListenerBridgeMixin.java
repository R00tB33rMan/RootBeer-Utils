package com.rootbeerutils.client.mixin;

import com.rootbeerutils.client.gui.CapturedFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerBridgeMixin extends ClientCommonPacketListenerImpl {

    protected ClientPacketListenerBridgeMixin(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
        super(minecraft, connection, cookie);
    }

    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void rbutils$captureOnRepeatLogin(CallbackInfo ci) {
        if (this.minecraft.isSameThread() && this.minecraft.level != null) {
            CapturedFrame.captureLastFrame();
        }
    }
}
