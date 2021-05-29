package com.m1zark.pixelmoncommands.commands;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.WT.WTPokemon;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.enums.ReceiveType;
import com.pixelmonmod.pixelmon.api.events.PixelmonReceivedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.PixelmonPlayerUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Storage implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        if (BattleRegistry.getBattle((EntityPlayerMP)src) != null) throw new CommandException(Text.of(TextColors.RED, "Cannot use this command while in battle!"));

        Optional<String> oplayer = args.getOne(Text.of("player"));
        Optional<String> oslots = args.getOne(Text.of("slots"));

        oplayer.ifPresent(player -> {
            if(oslots.isPresent()) {
                String[] slots = oslots.get().split(" ");

                User temp = getUser(player);
                if (temp == null) {
                    Chat.sendMessage(src, "&7Could not find a player named &b" + player + "&7! Check the name and try again.");
                    return;
                }

                for(String slot: slots) {
                    int s = Integer.parseInt(slot.replaceAll("[^0-9]", ""));
                    if (s < 1 || s > 6) {
                        Chat.sendMessage(src, "&c"+s+" &7is invalid... needs to be between 1 and 6.");
                        return;
                    }

                    PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) src);
                    if (storage != null) {
                        storage.retrieveAll();

                        Pokemon pokemon = storage.get(s - 1);

                        if(pokemon != null) {
                            if (storage.getTeam().size() <= 1 && !pokemon.isEgg()) {
                                Chat.sendMessage(src, "&7You can't send your last able Pok\u00E9mon.");
                                return;
                            }

                            storage.set(s - 1, null);
                            PixelmonCommands.getInstance().getSql().addStoragePokemon(temp.getUniqueId(), new WTPokemon(0, PixelmonUtils.getNbt(pokemon).toString()));
                        } else {
                            Chat.sendMessage(src, "&7There was an issue getting the Pok\u00E9mon in slot &b" + s + "&7! Check the slot and try again.");
                        }
                    }
                }

                Chat.sendMessage(src, "&7Pok\u00E9mon have been saved to storage. &b" +player+ " &7can retrieve them at there leisure.");
            }
        });

        return CommandResult.success();
    }

    public static class claimPokemon implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

            List<WTPokemon> pokemon = PixelmonCommands.getInstance().getSql().getStoragePokemon(((Player)src).getUniqueId());
            EntityPlayerMP player = (EntityPlayerMP) src;

            if(!pokemon.isEmpty()) {
                PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player);

                pokemon.forEach(pkm -> {
                    if(storage != null) {
                        if (BattleRegistry.getBattle(player) == null) {
                            storage.add(pkm.getPokemon());
                        } else {
                            Pixelmon.storageManager.getPCForPlayer(player).add(pkm.getPokemon());
                        }

                        Pixelmon.EVENT_BUS.post(new PixelmonReceivedEvent((EntityPlayerMP)src, ReceiveType.Command, pkm.getPokemon()));
                    }
                });

                PixelmonCommands.getInstance().getSql().removeStoragePokemon(((Player)src).getUniqueId());
            } else {
                Chat.sendMessage(src, "&7There are no Pok\u00E9mon in storage for you to retrieve.");
            }

            return CommandResult.success();
        }
    }

    public static class listStorage implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

            LinkedHashMap<WTPokemon,String> storage = PixelmonCommands.getInstance().getSql().getStorage();

            if(!storage.isEmpty()) {
                List<Text> lists = Lists.newArrayList();

                storage.forEach((k,v) -> {
                    User temp = getUsed(UUID.fromString(v));
                    String name = temp != null ? temp.getName() : v;
                    lists.add( Text.of(Chat.embedColours("&7" + name + " &f\u21E8 &d" + k.getPokemon().getSpecies().name())));
                });

                PaginationList.builder().contents(lists)
                        .title(Text.of(Chat.embedColours("&7Viewing Storage")))
                        .build()
                        .sendTo(src);
            } else {
                Chat.sendMessage(src, "&7There are no Pok\u00E9mon currently pending in storage.");
            }

            return CommandResult.success();
        }
    }

    private User getUser(String name) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        Optional<User> oUser = userStorage.get().get(name);

        return oUser.orElse(null);
    }

    private static User getUsed(UUID uuid) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        Optional<User> oUser = userStorage.get().get(uuid);

        return oUser.orElse(null);
    }
}
