package com.m1zark.pixelmoncommands.commands;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.enums.EnumMegaPokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.item.Item;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

public class Mega implements CommandExecutor {
    private static List<EnumMegaPokemon> megaPokemon = Lists.newArrayList();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            Optional<Player> player = args.getOne(Text.of("player"));
            player.ifPresent(p -> {
                Optional<Integer> quantity = args.getOne(Text.of("quantity"));
                int q = quantity.orElse(1);

                int Index = RandomHelper.getRandomNumberBetween(0, megaPokemon.size());
                ItemStack ItemStack = randomMega(megaPokemon.get(Index).getMegaEvoItems());

                if(!Inventories.giveItem(p, ItemStack, q)) {
                    Chat.sendMessage(src, "&cCouldn't give item to " + p.getName() + " because of a full inventory and enderchest");
                    return;
                }

                Chat.sendMessage(src, MainConfig.getMessages("Messages.RandomMega.Success")
                        .replace("{count}", String.valueOf(q))
                        .replace("{stone_name}", Inventories.getItemName(ItemStack).toPlain() + (q==1 ? "" : "s")));
            });

            return CommandResult.success();
    }

    private ItemStack randomMega(Item[] mega_stone) {
        int index = mega_stone.length==1 ? 0 : RandomHelper.getRandomNumberBetween(0, 1);

        net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(mega_stone[index]);
        return ItemStackUtil.fromNative(item);
    }

    static {
        for(EnumSpecies e : EnumSpecies.values()) {
            if(e.hasMega()) megaPokemon.add(EnumMegaPokemon.getMega(e));
        }
    }
}

