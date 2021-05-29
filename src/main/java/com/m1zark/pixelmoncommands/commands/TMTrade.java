package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.config.PixelmonItemsTMs;
import com.pixelmonmod.pixelmon.items.ItemTM;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TMTrade implements CommandExecutor {
    public static Map<String, Long> TMTradeCooldowns = new ConcurrentHashMap<>();

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, "You must be logged onto the server to run this command."));
        Player player = (Player) src;

        if(TMTradeCooldowns.containsKey(player.getUniqueId().toString())) {
            Time time = new Time(TMTradeCooldowns.get(player.getUniqueId().toString()));
            String expires = time.toString("%dd %dh %dm %ds");
            if(!expires.equalsIgnoreCase("Expired")) throw new CommandException(Chat.embedColours(MainConfig.getMessages("Messages.Cooldown").replace("{time}", expires)));
        }

        Optional<ItemStack> heldItem = player.getItemInHand(HandTypes.MAIN_HAND);

        if(!heldItem.isPresent() || !(heldItem.get().getType() instanceof ItemTM)) {
            throw new CommandException(Chat.embedColours("&cYou need to be holding a TM to be able to trade!"));
        }

        Inventories.removeItem(player, heldItem.get(), 1);

        ItemStack ItemStack = randomTM();
        if(!Inventories.giveItem(player, ItemStack, 1)) {
            throw new CommandException(Chat.embedColours("&cCouldn't give item to " + player.getName() + " because of a full inventory and enderchest"));
        }

        Chat.sendMessage(player, MainConfig.getMessages("Messages.TMTrade.Success").replace("{tm}", Inventories.getItemName(ItemStack).toPlain()));
        TMTradeCooldowns.put(player.getUniqueId().toString(), Instant.now().plusSeconds(MainConfig.getCooldownTimes("TMTrade")).toEpochMilli());
        return CommandResult.success();
    }

    private ItemStack randomTM(){
        return ItemStackUtil.fromNative(PixelmonUtils.randomTM());
    }
}
