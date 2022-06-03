package team.creative.cmdcam.common.command.argument;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import team.creative.cmdcam.common.scene.mode.CamMode;

public class CamModeArgument implements ArgumentType<String> {
    
    public static CamModeArgument mode() {
        return new CamModeArgument();
    }
    
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        final int start = reader.getCursor();
        final String result = reader.readString();
        if (CamMode.REGISTRY.get(result) == null) {
            reader.setCursor(start);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage("Invalid mode")), new TranslatableComponent("invalid_mode"));
        }
        
        return result;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggest(CamMode.REGISTRY.keys(), builder) : Suggestions.empty();
    }
    
    @Override
    public Collection<String> getExamples() {
        return CamMode.REGISTRY.keys();
    }
    
}