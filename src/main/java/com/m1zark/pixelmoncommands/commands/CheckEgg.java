package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Stats;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public class CheckEgg implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));
        EntityPlayerMP pl = (EntityPlayerMP) src;

        Optional<Integer> slot = args.getOne(Text.of("slot"));
        slot.ifPresent(s -> {
            if(slot.get() < 1 || slot.get() > 6) {
                Chat.sendMessage(src, Chat.embedColours("&cYou must enter a slot number between 1 and 6."));
                return;
            }

            PlayerPartyStorage pStorage = PixelmonUtils.getPlayerStorage(pl);
            Pokemon pokemon = pStorage.get(s - 1);

            if(pokemon == null || !pokemon.isEgg()) {
                Chat.sendMessage(src, Chat.embedColours("&cEither there is nothing in that slot, or this Pokemon isn't an egg."));
                return;
            }

            Chat.sendMessage(src, buildPokemonStats(pokemon));
        });



        return CommandResult.success();
    }

    private static Text buildPokemonStats(Pokemon pokemon) {
        String displayName = PixelmonUtils.updatePokemonName(pokemon.getSpecies().name());
        Stats stats = pokemon.getStats();
        Gender gender = pokemon.getGender();
        EVStore eVsStore = null;
        IVStore ivStore = null;
        EnumNature nature = pokemon.getNature();
        Boolean isShiny = pokemon.isShiny();
        Moveset moveset = pokemon.getMoveset();
        String formName = "";
        if (!(pokemon.getFormEnum().equals(EnumSpecial.Base) || pokemon.getFormEnum().getLocalizedName().equals("None") || pokemon.getFormEnum().getLocalizedName().equals("Standard") || pokemon.getFormEnum().getLocalizedName().equals("Normal"))) {
            formName = pokemon.getFormEnum().getLocalizedName();
        }
        TextColor nameColor = isShiny != false ? TextColors.GOLD : TextColors.DARK_AQUA;
        String pokeName = "&3" + displayName;
        int ivSum = 0;
        int evSum = 0;
        if (stats != null) {
            eVsStore = stats.evs;
            ivStore = stats.ivs;
            ivSum = ivStore.hp + ivStore.attack + ivStore.defence + ivStore.specialAttack + ivStore.specialDefence + ivStore.speed;
            evSum = eVsStore.hp + eVsStore.attack + eVsStore.defence + eVsStore.specialAttack + eVsStore.specialDefence + eVsStore.speed;
        }
        String pokeGender = gender.toString().equals("Female") ? "&d" + gender.toString() + " \u2640" : (gender.toString().equals("Male") ? "&b" + gender.toString() + " \u2642" : "&8Genderless \u26a5");
        ArrayList<String> moves = new ArrayList<String>();
        moves.add(moveset.get(0) == null ? "&bNone" : "&b" + moveset.get(0).getActualMove().getLocalizedName());
        moves.add(moveset.get(1) == null ? "&bNone" : "&b" + moveset.get(1).getActualMove().getLocalizedName());
        moves.add(moveset.get(2) == null ? "&bNone" : "&b" + moveset.get(2).getActualMove().getLocalizedName());
        moves.add(moveset.get(3) == null ? "&bNone" : "&b" + moveset.get(3).getActualMove().getLocalizedName());
        DecimalFormat df = new DecimalFormat("#0.##");

        String pokeStats = pokeName + " &7| &eLvl " + pokemon.getLevel() + " " + ((isShiny) ? "&7(&6Shiny&7)&r " : "") + "\n&r" +
                (!formName.isEmpty() ? "&7Form: &e" + WordUtils.capitalizeFully(formName) + "\n&r" : "") +
                "&7Ability: &e" + pokemon.getAbility().getName() + ((PixelmonUtils.isHiddenAbility(pokemon,pokemon.getAbility().getName())) ? " &7(&6HA&7)&r" : "") + "\n&r" +
                "&7Nature: &e" + nature.toString() + " &7(&a+" + PixelmonUtils.getNatureShorthand(nature.increasedStat) + " &7| &c-" + PixelmonUtils.getNatureShorthand(nature.decreasedStat) + "&7)" + "\n&r" +
                "&7Gender: " + pokeGender + "\n&r" +
                "&7Size: &e" + pokemon.getGrowth().toString() + "\n&r" +
                "&7Hidden Power: &e" + HiddenPower.getHiddenPowerType(pokemon.getIVs()).getLocalizedName() + "\n&r" +
                "&7Caught Ball: &e" + pokemon.getCaughtBall().getItem().getLocalizedName() + "\n\n&r" +

                "&7IVs: &e" + ivSum + "&7/&e186 &7(&a" + df.format((int)(((double)ivSum/186)*100)) + "%&7) \n"
                + "&cHP: " + ivStore.hp + " &7/ "
                + "&6Atk: " + ivStore.attack + " &7/ "
                + "&eDef: " + ivStore.defence + " &7/ "
                + "&9SpA: " + ivStore.specialAttack + " &7/ "
                + "&aSpD: " + ivStore.specialDefence + " &7/ "
                + "&dSpe: " + ivStore.speed + "\n" +
                "&7EVs: &e" + evSum + "&7/&e510 &7(&a" + df.format((int)(((double)evSum/510)*100)) + "%&7) \n"
                + "&cHP: " + eVsStore.hp + " &7/ "
                + "&6Atk: " + eVsStore.attack + " &7/ "
                + "&eDef: " + eVsStore.defence + " &7/ "
                + "&9SpA: " + eVsStore.specialAttack + " &7/ "
                + "&aSpD: " + eVsStore.specialDefence + " &7/ "
                + "&dSpe: " + eVsStore.speed + "\n\n" +

                "&7Moves:\n" + moves.get(0) + " &7- " + moves.get(1) + " &7- " + moves.get(2) + " &7- " + moves.get(3);

        return Text.builder().color(nameColor)
                .append(Text.of(Chat.embedColours("[" + displayName + "]")))
                .onHover(TextActions.showText(Text.of(Chat.embedColours(pokeStats))
                )).build();
    }

}
