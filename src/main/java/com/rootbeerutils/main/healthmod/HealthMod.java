package com.rootbeerutils.main.healthmod;

import com.mojang.brigadier.arguments.StringArgumentType;


import com.rootbeerutils.main.util.PlayerSuggestionProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthMod implements ModInitializer {

    public static final String MOD_ID = "HealthMod";

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("HealthMod-RBU");

    public static Logger getLogger() {
        return LOGGER;
    }

    private void healPlayer (ServerPlayer player) {
        player.setHealth(player.getMaxHealth());
        player.setAbsorptionAmount(20);
        player.setRemainingFireTicks(0);
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20f);
    }

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("heal")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        healPlayer(player);
                        context.getSource().sendSuccess(() -> Component.literal("You healed yourself!"), false);
                        return 1;
                    })
                    .then(Commands.argument("player_name", StringArgumentType.string())
                            .suggests(new PlayerSuggestionProvider())
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "player_name");
                                ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(name);
                                if(player == null) {
                                    context.getSource().sendFailure(Component.literal("Player not found. Wrong name?"));
                                    return 0;
                                }
                                healPlayer(player);
                                context.getSource().sendSuccess(() -> Component.literal("Healed " + name + "!"), false);
                                return 1;
                            })));
        });
        LOGGER.info(MOD_ID + " Loaded!");
    }
}
