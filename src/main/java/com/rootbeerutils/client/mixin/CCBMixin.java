package com.rootbeerutils.client.mixin;

import com.rootbeerutils.main.customclientbrand.config.CCBConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrandPayload.class)
public class CCBMixin {

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "HEAD"), cancellable = true)
    public void changeClientBrand(FriendlyByteBuf buf, CallbackInfo ci)  {
        String customBrand = AutoConfig.getConfigHolder(CCBConfig.class).get().customBrand();
        buf.writeUtf(customBrand);
        ci.cancel();
    }
}
