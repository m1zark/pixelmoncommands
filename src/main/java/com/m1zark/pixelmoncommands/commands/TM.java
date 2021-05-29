package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.config.PixelmonItemsTMs;
import com.pixelmonmod.pixelmon.items.PixelmonItem;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Optional;

public class TM implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Player> player = args.getOne(Text.of("player"));
        player.ifPresent(p -> {
            Optional<Integer> quantity = args.getOne(Text.of("quantity"));
            int q = quantity.orElse(1);

            ItemStack ItemStack = randomTM();

            if(!Inventories.giveItem(p, ItemStack, q)) {
                Chat.sendMessage(src, "&cCouldn't give item to " + p.getName() + " because of a full inventory and enderchest");
                return;
            }

            Chat.sendMessage(p, MainConfig.getMessages("Messages.RandomTM.Success")
                    .replace("{count}",String.valueOf(q))
                    .replace("{tm}", Inventories.getItemName(ItemStack).toPlain() + (q==1 ? "" : "s")));
        });

        return CommandResult.success();
    }

    private ItemStack randomTM() {
        return ItemStackUtil.fromNative(PixelmonUtils.randomTM());
    }
}
