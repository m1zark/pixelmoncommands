package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.enums.ReceiveType;
import com.pixelmonmod.pixelmon.api.events.PixelmonReceivedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.PixelmonPlayerUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RL implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Player> player = args.getOne(Text.of("player"));
        player.ifPresent(p -> giveLegend(args, p));

        return CommandResult.success();
    }

    private void giveLegend(CommandContext args, Player p) {
        List<String> legendaries = EnumSpecies.legendaries;
        legendaries.addAll(EnumSpecies.ultrabeasts);

        legendaries.removeIf(name -> name.equals("Phione") || name.equals("Meltan") || name.equals("Melmetal"));

        String str = legendaries.get(new Random().nextInt(legendaries.size() - 1));
        String shiny = (args.hasAny("s")) ? "s" : "";

        String[] specs = {str,shiny};

        EntityPlayerMP player = PixelmonPlayerUtils.getUniquePlayerStartingWith(p.getName());

        PokemonSpec spec = PokemonSpec.from(specs);
        spec.boss = null;
        Pokemon pokemon = spec.create();
        pokemon.setCaughtBall(EnumPokeballs.PokeBall);

        PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage(player);
        if (storage != null) {
            Pixelmon.EVENT_BUS.post(new PixelmonReceivedEvent(player, ReceiveType.Command, pokemon));
            if (BattleRegistry.getBattle(player) == null) {
                storage.add(pokemon);
            } else {
                Pixelmon.storageManager.getPCForPlayer(player).add(pokemon);
            }

            Chat.sendMessage(p, MainConfig.getMessages("Messages.RandomLegend.Success").replace("{pokemon}", str));
        }else{
            Chat.sendMessage(p, "&cInvalid player: " + p.getName());
        }
    }
}