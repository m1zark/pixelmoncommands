package com.m1zark.pixelmoncommands.listeners;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.WT.WTPokemon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.title.Title;

import java.util.List;

public class PlayerListeners {
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        List<WTPokemon> pokemon = PixelmonCommands.getInstance().getSql().getStoragePokemon(player.getUniqueId());

        if(!pokemon.isEmpty()) {
            Chat.sendMessage(player, "&cYou have Pok\u00E9mon waiting for you... use &7/storage claim &cto retrieve them!");
        }
    }

    @Listener public void OnItemUse(InteractItemEvent.Secondary.MainHand event, @Root Player player) {
        if (!player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) { return; }
        ItemStack heldItem = player.getItemInHand(HandTypes.MAIN_HAND).get();

        if (Inventories.doesHaveNBT(heldItem,"disguise")) {
            String[] name = Inventories.getItemName(heldItem).toPlain().trim().split("\\s+");
            if (name.length == 3) {
                if(!player.hasPermission("pd.shiny." + name[1].toLowerCase())) {
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "lp user " + player.getName() + " permission set pd.shiny." + name[1].toLowerCase() + " true");
                    Chat.sendMessage(player, "&aYou have unlocked the &bShiny " + name[1] + " Disguise!");
                    Inventories.removeItem(player, player.getItemInHand(HandTypes.MAIN_HAND).get(), 1);
                }else{
                    Chat.sendMessage(player, "&cYou have already unlocked the &bShiny " + name[1] + " Disguise!");
                }
            } else {
                if(!player.hasPermission("pd." + name[0].toLowerCase())) {
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "lp user " + player.getName() + " permission set pd." + name[0].toLowerCase() + " true");
                    Chat.sendMessage(player, "&aYou have unlocked the &b" + name[0] + " Disguise!");
                    Inventories.removeItem(player, player.getItemInHand(HandTypes.MAIN_HAND).get(), 1);
                }else{
                    Chat.sendMessage(player, "&cYou have already unlocked the &b" + name[0] + " Disguise!");
                }
            }
        } else if(Inventories.doesHaveNBT(heldItem, "xp")) {
            String[] data = heldItem.toContainer().getString(DataQuery.of("UnsafeData","xp")).get().split(":");
            switch (data[0]) {
                case "player": {
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "lp user " + player.getName() + " meta settemp pokexp_value 2 " + data[1]);
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "lp user " + player.getName() + " perm settemp pokexpmultiplier.enable true " + data[1]);
                    Title send1 = Title.builder().title(Chat.embedColours("&aDouble XP Unlocked!")).subtitle(Chat.embedColours("&7You enabled double xp for &b" + data[1])).fadeIn(15).stay(50).fadeOut(15).build();
                    player.sendTitle(send1);
                    break;
                }
                case "all": {
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "lp group youngster meta settemp pokexp_value 2 " + data[1]);
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "lp group youngster perm settemp pokexpmultiplier.enable true " + data[1]);
                    Title send2 = Title.builder().title(Chat.embedColours("&aDouble XP Unlocked!")).subtitle(Chat.embedColours("&b" + player.getName() + " &7enabled server-wide double xp for &b" + data[1])).fadeIn(15).stay(50).fadeOut(15).build();

                    for (Player OnlinePlayer : Sponge.getServer().getOnlinePlayers()) OnlinePlayer.sendTitle(send2);
                    break;
                }
            }
            player.playSound(SoundTypes.BLOCK_ENCHANTMENT_TABLE_USE, player.getLocation().getPosition(), 1.0);
            Inventories.removeItem(player, player.getItemInHand(HandTypes.MAIN_HAND).get(), 1);
        }
    }
}
