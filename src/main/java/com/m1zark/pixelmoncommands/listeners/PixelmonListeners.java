package com.m1zark.pixelmoncommands.listeners;

import com.m1zark.m1utilities.M1utilities;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Discord.Message;
import com.m1zark.m1utilities.api.Money;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.BattleBets;
import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnLocationEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import com.pixelmonmod.pixelmon.enums.forms.EnumMega;
import com.pixelmonmod.pixelmon.enums.forms.EnumPrimal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.spongepowered.api.entity.living.player.Player;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class PixelmonListeners {
    @SubscribeEvent
    public void pokemonCapture(CaptureEvent.SuccessfulCapture event) {
        Player player = (Player) event.player;
        Pokemon poke = event.getPokemon().getPokemonData();

        if ((poke.isShiny() && MainConfig.enableMessages("General.Announcements.enableShiny")) || (EnumSpecies.legendaries.contains(poke.getSpecies().name()) && MainConfig.enableMessages("General.Announcements.enableLegendary"))) {
            Chat.sendServerWideMessage(
                    MainConfig.getMessages("Messages.Announcements.displayMessage")
                            .replace("{player}", player.getName())
                            .replace("{action}","captured")
                            .replace("{shiny}",poke.isShiny() ? "Shiny " : "")
                            .replace("{legendary}", EnumSpecies.legendaries.contains(poke.getSpecies().name()) ? "Legendary" : EnumSpecies.ultrabeasts.contains(poke.getSpecies().name()) ? "Ultrabeast" : "")
                            .replace("{boss}", "")
                            .replace("{pokemon}", poke.getSpecies().name())
            );
        }
    }

    @SubscribeEvent
    public void onBattleEnd(BeatWildPixelmonEvent event) {
        Player p = (Player) event.player;

        for (PixelmonWrapper wrapper : event.wpp.allPokemon) {
            EntityPixelmon pokemon =  wrapper.entity;

            if(pokemon.getTrainer() == null && !pokemon.hasOwner() && (pokemon.getFormEnum() == EnumMega.Mega || pokemon.getFormEnum() == EnumPrimal.PRIMAL || pokemon.isBossPokemon() || EnumSpecies.legendaries.contains(pokemon.getSpecies().name()) || EnumSpecies.ultrabeasts.contains(pokemon.getSpecies().name())) && MainConfig.enableMessages("General.Announcements.enableBoss")) {
                Chat.sendServerWideMessage(
                        MainConfig.getMessages("Messages.Announcements.displayMessage")
                                .replace("{player}", p.getName())
                                .replace("{action}", "defeated")
                                .replace("{shiny}", pokemon.getPokemonData().isShiny() ? "Shiny " : "")
                                .replace("{bossmode}", pokemon.getBossMode().getLocalizedName())
                                .replace("{legendary}", EnumSpecies.legendaries.contains(pokemon.getSpecies().name()) ? "Legendary" : EnumSpecies.ultrabeasts.contains(pokemon.getSpecies().name()) ? "Ultrabeast" : "")
                                .replace("{boss}", pokemon.getFormEnum() == EnumMega.Mega ? "Mega" : pokemon.getFormEnum() == EnumPrimal.PRIMAL ? "Primal" : pokemon.isBossPokemon() ? "Boss" : "")
                                .replace("{pokemon}", pokemon.getSpecies().name())
                );
            }
        }
    }

    /*
    @SubscribeEvent(priority= EventPriority.HIGHEST)
    public void onSpawn(SpawnEvent spawnEvent) {
        if (spawnEvent.action instanceof SpawnActionPokemon) {
            EntityPixelmon pixelmon = (EntityPixelmon)spawnEvent.action.getOrCreateEntity();
            EnumSpecies species = pixelmon.getSpecies();

            if(ConfigManager.enableMessages("General.Misc.enableSpawnFix")) {
                if (!pixelmon.isBossPokemon() && EnumSpecies.legendaries.contains(species.name) && !RandomHelper.getRandomChance((float) PixelmonConfig.legendarySpawnChance)) {
                    spawnEvent.setCanceled(true);
                }

                if (pixelmon.isBossPokemon() && pixelmon.getFormEnum() == EnumMega.Mega && !RandomHelper.getRandomChance(PixelmonConfig.bossSpawnChance)) {
                    spawnEvent.setCanceled(true);
                }
            }
        }
    }
    */

    @SubscribeEvent
    public void onPokemonSpawn(SpawnEvent event) {
        final Entity spawnedEntity = event.action.getOrCreateEntity();
        if (spawnedEntity instanceof EntityPixelmon) {
            final EntityPixelmon pokemon = (EntityPixelmon) spawnedEntity;

            if (EnumSpecies.legendaries.contains(pokemon.getPokemonName()) && pokemon.getFormEnum() != EnumMega.Mega && !pokemon.isBossPokemon() && !pokemon.hasOwner()) {
                MainConfig.saveSpawn(pokemon.getLocalizedName() + ":" + Instant.now().toEpochMilli());
                MainConfig.saveConfig();

                if(MainConfig.enableMessages("General.Announcements.enableDiscordNotifications")) {
                    M1utilities.getInstance().getDiscordNotifier().ifPresent(notifier -> {
                        Message message = notifier.forgeMessage(MainConfig.discordOption("legendary-spawn",pokemon.getPokemonName(), getBiomeName(event.action.spawnLocation.biome)));
                        notifier.sendMessage(message);
                    });
                }
            }

            if(EnumSpecies.ultrabeasts.contains(pokemon.getPokemonName()) && !pokemon.isBossPokemon() && !pokemon.hasOwner()) {
                MainConfig.saveSpawn(pokemon.getLocalizedName() + ":" + Instant.now().toEpochMilli());
                MainConfig.saveConfig();

                Chat.sendServerWideMessage("&f[&dPixelmon&f] &a" + pokemon.getPokemonName() + " has spawned in a " + getBiomeName(event.action.spawnLocation.biome) + " biome");

                if(MainConfig.enableMessages("General.Announcements.enableDiscordNotifications")) {
                    M1utilities.getInstance().getDiscordNotifier().ifPresent(notifier -> {
                        Message message = notifier.forgeMessage(MainConfig.discordOption("ultra-spawn",pokemon.getPokemonName(), getBiomeName(event.action.spawnLocation.biome)));
                        notifier.sendMessage(message);
                    });
                }
            }

            if(pokemon.getFormEnum() == EnumMega.Mega || pokemon.getFormEnum() == EnumPrimal.PRIMAL && pokemon.isBossPokemon() && !pokemon.hasOwner()) {
                if(MainConfig.enableMessages("General.Announcements.enableDiscordNotifications")) {
                    M1utilities.getInstance().getDiscordNotifier().ifPresent(notifier -> {
                        Message message = notifier.forgeMessage(MainConfig.discordOption("mega-spawn",pokemon.getPokemonName(), getBiomeName(event.action.spawnLocation.biome)));
                        notifier.sendMessage(message);
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public void onSpawn(SpawnLocationEvent event) {
        if(PixelmonCommands.getInstance().getAfkService() != null && MainConfig.enableMessages("General.Misc.disableAFKSpawns")) {
            if (event.getSpawnLocation().cause instanceof EntityPlayerMP && PixelmonCommands.getInstance().getAfkService().isAFK((Player) event.getSpawnLocation().cause)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLegendarySpawn(LegendarySpawnEvent.ChoosePlayer event) {
        if(PixelmonCommands.getInstance().getAfkService() != null && MainConfig.enableMessages("General.Misc.disableAFKSpawns")) {
            if (PixelmonCommands.getInstance().getAfkService().isAFK((Player) event.player)) {
                event.setCanceled(true);
            }
        }
    }

    private static String getBiomeName(Biome biome) {
        String name = "";
        try {
            Field f = ReflectionHelper.findField(Biome.class, "biomeName", "field_185412_a", "field_76791_y");
            name = (String) f.get(biome);
        } catch (Exception e) {
            return "Error getting biome name";
        }

        return name;
    }


    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        if(event.cause == EnumBattleEndCause.NORMAL || event.cause == EnumBattleEndCause.FORFEIT) {
            for(Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet()) {
                if(entry.getKey() instanceof PlayerParticipant) {
                    PlayerParticipant playerParticipant = (PlayerParticipant) entry.getKey();

                    Optional<BattleBets> battle = PixelmonCommands.getInstance().battleBets.stream().filter(bet -> event.getPlayers().contains((EntityPlayerMP)bet.player1) || event.getPlayers().contains((EntityPlayerMP)bet.player2)).findFirst();

                    Player winner = null, loser = null;
                    if(battle.isPresent()) {
                        switch (entry.getValue()) {
                            case VICTORY:
                                if(playerParticipant.player.equals((EntityPlayerMP)battle.get().player1)) {
                                    Money.transfer(battle.get().player2.getUniqueId(), battle.get().player1.getUniqueId(), battle.get().betAmount);
                                    winner = battle.get().player1;
                                    loser = battle.get().player2;
                                } else if(playerParticipant.player.equals((EntityPlayerMP)battle.get().player2)) {
                                    Money.transfer(battle.get().player1.getUniqueId(), battle.get().player2.getUniqueId(), battle.get().betAmount);
                                    winner = battle.get().player2;
                                    loser = battle.get().player1;
                                }

                                break;
                            case DEFEAT:
                                break;
                        }

                        Chat.sendMessage(winner, "Congratulations you won! P" + battle.get().betAmount + " has been added to your account.");
                        Chat.sendMessage(loser, "You lost the bet... P" + battle.get().betAmount + " has been withdrawn from your account.");
                        PixelmonCommands.getInstance().battleBets.remove(battle.get());
                    }
                }
            }
        }
    }
}
