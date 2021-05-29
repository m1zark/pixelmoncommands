package com.m1zark.pixelmoncommands.commands;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.enums.items.EnumZCrystals;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

public class ZCrystal implements CommandExecutor {
    private static List<String> zCrystals = Lists.newArrayList();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Player> player = args.getOne(Text.of("player"));
        player.ifPresent(p -> {
            Optional<Integer> quantity = args.getOne(Text.of("quantity"));
            int q = quantity.orElse(1);

            ItemStack ItemStack = randomCrystal();

            if(!Inventories.giveItem(p, ItemStack, q)) {
                Chat.sendMessage(src, "&cCouldn't give item to " + p.getName() + " because of a full inventory and enderchest");
                return;
            }

            Chat.sendMessage(p, MainConfig.getMessages("Messages.RandomZCrystal.Success")
                    .replace("{count}",String.valueOf(q))
                    .replace("{crystal_name}", Inventories.getItemName(ItemStack).toPlain() + (q==1 ? "" : "s")));
        });

        return CommandResult.success();
    }

    private ItemStack randomCrystal() {
        int index = RandomHelper.getRandomNumberBetween(0, zCrystals.size() - 1);
        return ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:"+zCrystals.get(index)).get()).build();
    }

    static {
        for(EnumZCrystals crystals : EnumZCrystals.values()) {
            zCrystals.add(crystals.getFileName());
        }
    }
}
