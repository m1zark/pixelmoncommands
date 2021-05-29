package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.Evolution;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.evolution.conditions.EvoCondition;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.Entity;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Evolve implements CommandExecutor {
    public static Map<String, Long> evolveCooldowns = new ConcurrentHashMap<>();

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        Optional<Player> playerOptional = args.getOne(Text.of("player"));
        if(playerOptional.isPresent()) {
            if (!playerOptional.get().equals(src) && !src.hasPermission("pixelcommands.others.evolve")) {
                throw new CommandException(Text.of(TextColors.RED, MainConfig.getMessages("Messages.Evolve.OtherPlayers")));
            }
        }

        if (BattleRegistry.getBattle((EntityPlayerMP) src) != null) {
            throw new CommandException(Text.of(TextColors.RED,"Cannot evolve this Pok\u00E9mon while you are in battle!"));
        }

        if(evolveCooldowns.containsKey(((Player) src).getUniqueId().toString())) {
            Time time = new Time(evolveCooldowns.get(((Player) src).getUniqueId().toString()));
            String expires = time.toString("%dd %dh %dm %ds");
            if(!expires.equalsIgnoreCase("Expired")) throw new CommandException(Chat.embedColours(MainConfig.getMessages("Messages.Cooldown").replace("{time}", expires)));
        }

        Optional<Integer> slot = args.getOne(Text.of("slot"));
        slot.ifPresent(s -> {
            if (slot.get() < 1 || slot.get() > 6) {
                Chat.sendMessage(src, MainConfig.getMessages("Messages.Evolve.InvalidNumber"));
                return;
            }

            EntityPlayerMP pl = (EntityPlayerMP) playerOptional.orElse((Player)src);
            PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage(pl);
            if (storage == null) { return; }

            if (storage.get(s - 1) != null) {
                Pokemon p = storage.get(s - 1);

                ArrayList<Evolution> evo = p.getBaseStats().evolutions;
                if (evo.size() <= 0) {
                    Chat.sendMessage(src, MainConfig.getMessages("Messages.Evolve.NoEvolutions"));
                    return;
                }

                if(MainConfig.getBlocked().contains(p.getSpecies().name)) {
                    Chat.sendMessage(src, MainConfig.getMessages("Messages.Evolve.blacklisted").replace("{pokemon}", p.getSpecies().name));
                    return;
                }

                if (EnumSpecies.hasPokemonAnyCase((evo.get(0)).to.name)) {
                    EntityPixelmon entityPixelmon = p.getOrSpawnPixelmon((Entity)src);
                    boolean evolved = true;
                    boolean force = false;
                    for (Evolution evolution : evo) {
                        if (this.evoPassesAll(evolution.conditions, entityPixelmon, force)) {
                            evolution.doEvolution(entityPixelmon);
                            evolved = true;
                            Pixelmon.EVENT_BUS.post(new EvolveEvent.PreEvolve((EntityPlayerMP) src, entityPixelmon, evolution));
                            break;
                        }
                        evolved = false;
                    }

                    if (!evolved) {
                        p.retrieve();
                        Chat.sendMessage(src, MainConfig.getMessages("Messages.Evolve.CantEvolve"));
                    }
                }
            }

            evolveCooldowns.put(((Player) src).getUniqueId().toString(), Instant.now().plusSeconds(MainConfig.getCooldownTimes("Evolve")).toEpochMilli());
        });

        return CommandResult.success();
    }

    private boolean evoPassesAll(ArrayList<EvoCondition> evoConditions, EntityPixelmon entityPixelmon, boolean force) {
        if (force) {
            return true;
        }
        for (EvoCondition e : evoConditions) {
            if (e.passes(entityPixelmon)) continue;
            return false;
        }
        return true;
    }
}