package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class Reload implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        PixelmonCommands.getInstance().getConfig().reload();

        Chat.sendMessage(src, "&7PixelmonCommands config successfully reloaded.");

        return CommandResult.success();
    }
}
