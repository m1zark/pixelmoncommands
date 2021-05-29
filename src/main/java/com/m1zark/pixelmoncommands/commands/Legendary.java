package com.m1zark.pixelmoncommands.commands;

import com.google.common.base.Strings;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.concurrent.TimeUnit;

public class Legendary implements CommandExecutor {

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, "You must be logged onto the server to run this command."));
        Player player = (Player) src;

        if(Strings.isNullOrEmpty(MainConfig.getLegendSpawn())) throw new CommandException(Chat.embedColours("&cNo legends have spawned yet.."));

        String[] spawn = MainConfig.getLegendSpawn().split(":");
        long millis = System.currentTimeMillis() - Long.parseLong(spawn[1]);
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24L;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60L;

        Chat.sendMessage(player, MainConfig.getMessages("Messages.LegendarySpawn.spawnMessage").replace("{name}",spawn[0]).replace("{time}", String.format("%dd %dh %dm", days, hours, minutes)));

        return CommandResult.success();
    }
}