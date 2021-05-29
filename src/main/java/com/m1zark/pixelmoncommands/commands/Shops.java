package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.NPCShopkeeper;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ServerNPCRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ShopkeeperData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class Shops implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        if (BattleRegistry.getBattle((EntityPlayerMP)src) != null) throw new CommandException(Text.of(TextColors.RED, "Cannot open shops while in battle!"));

        Optional<String> shopID = args.getOne(Text.of("shopID"));
        EntityPlayerMP player = (EntityPlayerMP) src;

        if(shopID.isPresent()) {
            ShopkeeperData data = shopData(shopID.get());

            if (data != null) {
                NPCShopkeeper shop = new NPCShopkeeper(player.world);
                shop.init(data);
                shop.interactWithNPC(player, EnumHand.MAIN_HAND);
            } else {
                Chat.sendMessage(src, "&7There was an error loading shop '&4" + shopID.get() + "&7'. Please check the config.");
            }
        }

        return CommandResult.success();
    }

    private static ShopkeeperData shopData(String id) {
        for (ShopkeeperData data : ServerNPCRegistry.getEnglishShopkeepers()) {
            if (!data.id.equalsIgnoreCase(id)) continue;
            return data;
        }

        return null;
    }
}