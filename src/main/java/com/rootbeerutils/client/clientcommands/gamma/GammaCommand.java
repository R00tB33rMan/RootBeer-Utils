package com.rootbeerutils.client.clientcommands.gamma;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;


import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public class GammaCommand implements ClientModInitializer {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("gamma")
                .then(argument("gamma", doubleArg())
                        .executes(ctx -> setGamma(ctx.getSource(), getDouble(ctx, "gamma")))));
    }

    private static int setGamma(FabricClientCommandSource source, double gamma) {
        try {
            var optionInstance = source.getClient().options.gamma();
            var field = net.minecraft.client.OptionInstance.class.getDeclaredField("value");
            field.setAccessible(true);
            field.set(optionInstance, gamma);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Component feedback = Component.translatable("commands.gamma.success", gamma);
        source.sendFeedback(feedback);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void onInitializeClient() {

    }
}

