package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import java.util.Optional;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

public class Give implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return CommandResult.success();
    }

    public static class xp implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Optional<Player> player = args.getOne(Text.of("player"));
            Optional<String> type = args.getOne(Text.of("type"));
            Optional<Integer> duration = args.getOne(Text.of("duration"));
            player.ifPresent(p -> {
                ItemStack item = PixelmonUtils.xpItem(type.orElse("player"), duration.orElse(1));
                if (!Inventories.giveItem(p, item, 1)) {
                    Chat.sendMessage(src, "&cCouldn't give item to " + p.getName() + " because of a full inventory.");
                }
            });
            return CommandResult.success();
        }
    }

    public static class disguise implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Optional<Player> player = args.getOne(Text.of("player"));
            player.ifPresent(p -> {
                ItemStack item = PixelmonUtils.disguiseItem(args.hasAny("s"), args.getOne("pokemon").get().toString());
                if(!Inventories.giveItem(p, item, 1)) {
                    Chat.sendMessage(src, "&cCouldn't give item to " + p.getName() + " because of a full inventory and enderchest");
                }
            });
            return CommandResult.success();
        }
    }
}
