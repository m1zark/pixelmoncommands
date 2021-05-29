package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.enums.ReceiveType;
import com.pixelmonmod.pixelmon.api.events.PixelmonReceivedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import com.pixelmonmod.pixelmon.items.ItemCoveredFossil;
import com.pixelmonmod.pixelmon.items.ItemFossil;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Fossil implements CommandExecutor {
    public static Map<String, Long> fossilCooldowns = new ConcurrentHashMap<>();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, "You must be logged onto the server to run this command."));
        Player player = (Player) src;

        if(fossilCooldowns.containsKey(((Player) src).getUniqueId().toString())) {
            Time time = new Time(fossilCooldowns.get(((Player) src).getUniqueId().toString()));
            String expires = time.toString("%dd %dh %dm %ds");
            if(!expires.equalsIgnoreCase("Expired")) throw new CommandException(Chat.embedColours(MainConfig.getMessages("Messages.Cooldown").replace("{time}", expires)));
        }

        if(player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            ItemType heldItem = player.getItemInHand(HandTypes.MAIN_HAND).get().getType();
            EnumSpecies pokemonName;

            if(heldItem instanceof ItemFossil){
                ItemFossil fossil = (ItemFossil) heldItem;
                pokemonName = fossil.fossil.getPokemon();
            } else {
                if(!(heldItem instanceof ItemCoveredFossil)) {
                    Chat.sendMessage(player, MainConfig.getMessages("Messages.Fossil.NotAFossil"));
                    return CommandResult.successCount(0);
                }

                ItemCoveredFossil fossil = (ItemCoveredFossil) heldItem;
                pokemonName = fossil.cleanedFossil.fossil.getPokemon();
            }

            Pokemon pokemon = Pixelmon.pokemonFactory.create(pokemonName);
            pokemon.getLevelContainer().setLevel(1);
            pokemon.setCaughtBall(EnumPokeballs.PokeBall);

            PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) player);
            if(storage != null) {
                storage.add(pokemon);

                Pixelmon.EVENT_BUS.post(new PixelmonReceivedEvent((EntityPlayerMP) player, ReceiveType.Command, pokemon));

                Chat.sendMessage(player, MainConfig.getMessages("Messages.Fossil.Success").replace("{pokemon}", pokemonName.name));

                Inventories.removeItem(player, player.getItemInHand(HandTypes.MAIN_HAND).get(), 1);

                fossilCooldowns.put(((Player) src).getUniqueId().toString(), Instant.now().plusSeconds(MainConfig.getCooldownTimes("Fossil")).toEpochMilli());
            }
        } else {
            Chat.sendMessage(player, MainConfig.getMessages("Messages.Fossil.NoItem"));
        }

        return CommandResult.success();
    }
}
