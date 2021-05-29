package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Hatch implements CommandExecutor {
    public static Map<String, Long> hatchCooldowns = new ConcurrentHashMap<>();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));
        EntityPlayerMP pl = (EntityPlayerMP) src;

        if(hatchCooldowns.containsKey(((Player) src).getUniqueId().toString())) {
            Time time = new Time(hatchCooldowns.get(((Player) src).getUniqueId().toString()));
            String expires = time.toString("%dd %dh %dm %ds");
            if(!expires.equalsIgnoreCase("Expired")) throw new CommandException(Chat.embedColours(MainConfig.getMessages("Messages.Cooldown").replace("{time}", expires)));
        }

        Optional<Integer> slot = args.getOne(Text.of("slot"));
        slot.ifPresent(s -> {
            if(slot.get() < 1 || slot.get() > 6) {
                Chat.sendMessage(src, MainConfig.getMessages("Messages.Hatch.InvalidNumber"));
                return;
            }

            PlayerPartyStorage pStorage = PixelmonUtils.getPlayerStorage(pl);
            Pokemon pokemon = pStorage.get(s - 1);

            if(pokemon == null) {
                Chat.sendMessage(src, MainConfig.getMessages("Messages.Hatch.EmptySlot"));
                return;
            }

            if (!pokemon.isEgg()) {
                Chat.sendMessage(src, MainConfig.getMessages("Messages.Hatch.NoEgg"));
                return;
            }

            pokemon.hatchEgg();

            Chat.sendMessage(src, MainConfig.getMessages("Messages.Hatch.Success").replace("{pokemon}", pokemon.getSpecies().name));
            hatchCooldowns.put(((Player) src).getUniqueId().toString(), Instant.now().plusSeconds(MainConfig.getCooldownTimes("Hatch")).toEpochMilli());
        });

        return CommandResult.success();
    }
}
