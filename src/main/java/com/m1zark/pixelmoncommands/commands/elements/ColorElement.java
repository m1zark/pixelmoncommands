package com.m1zark.pixelmoncommands.commands.elements;

import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ColorElement extends CommandElement {
    public ColorElement(Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource commandSource, CommandArgs commandArgs) throws ArgumentParseException {
        return commandArgs.next();
    }

    @Override
    public List<String> complete(CommandSource commandSource, CommandArgs commandArgs, CommandContext commandContext) {
        final List<String> formats = new ArrayList<>();

        for (TextFormatting format : TextFormatting.values()) {
            formats.add(format.getFriendlyName());
        }

        formats.removeIf(name -> name.equalsIgnoreCase("obfuscated"));
        formats.removeIf(name -> name.equalsIgnoreCase("bold"));
        formats.removeIf(name -> name.equalsIgnoreCase("strikethrough"));
        formats.removeIf(name -> name.equalsIgnoreCase("italic"));
        formats.removeIf(name -> name.equalsIgnoreCase("underline"));

        return formats;
    }
}
