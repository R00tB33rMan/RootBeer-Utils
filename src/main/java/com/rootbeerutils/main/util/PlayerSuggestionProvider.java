package com.rootbeerutils.main.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        CommandSourceStack source = context.getSource();

        Collection<String> playerNames = source.getOnlinePlayerNames();

        // Add all player names to the builder.
        for (String playerName : playerNames) {
            builder.suggest(playerName);
        }

        // Lock the suggestions after we've modified them.
        return builder.buildFuture();
    }

}
