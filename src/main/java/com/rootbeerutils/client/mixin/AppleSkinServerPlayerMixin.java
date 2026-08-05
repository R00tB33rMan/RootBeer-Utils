package com.rootbeerutils.client.mixin;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.rootbeerutils.client.appleskin.network.SyncHandler;

@Mixin(ServerPlayer.class)
public abstract class AppleSkinServerPlayerMixin extends Entity
{
    public AppleSkinServerPlayerMixin(EntityType<?> entityType, Level world)
    {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    void onUpdate(CallbackInfo info)
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        SyncHandler.onPlayerUpdate(player);
    }
}
