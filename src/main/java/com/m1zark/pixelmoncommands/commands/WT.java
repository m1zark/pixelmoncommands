package com.m1zark.pixelmoncommands.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.WT.WTPokemon;
import com.m1zark.pixelmoncommands.WT.WonderTradeUI;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.enums.ReceiveType;
import com.pixelmonmod.pixelmon.api.events.PixelmonReceivedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.*;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import de.waterdu.aquaauras.helper.FileHelper;
import de.waterdu.aquaauras.structures.AuraDefinition;
import de.waterdu.aquaauras.structures.EffectDefinition;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WT implements CommandExecutor {
    public static Map<String, Long> WTCooldowns = new ConcurrentHashMap<>();

    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        if (BattleRegistry.getBattle((EntityPlayerMP)src) != null) throw new CommandException(Text.of(TextColors.RED, "Cannot use WonderTrade while in battle!"));

        if(WTCooldowns.containsKey(((Player) src).getUniqueId().toString())) {
            Time time = new Time(WTCooldowns.get(((Player) src).getUniqueId().toString()));
            String expires = time.toString("%dd %dh %dm %ds");
            if(!expires.equalsIgnoreCase("Expired")) throw new CommandException(Chat.embedColours(MainConfig.getMessages("Messages.Cooldown").replace("{time}", expires)));
        }

        PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) src);
        if(storage != null) {
            storage.retrieveAll();
            ((Player) src).openInventory((new WonderTradeUI((Player) src)).getInventory());
        }

        return CommandResult.success();
    }

    public static class WTAdmin implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

            Optional<String> type = args.getOne(Text.of("type"));
            Optional<Integer> slot = args.getOne(Text.of("slot"));

            if(type.isPresent()) {
                if(type.get().equals("view")) {
                    List<Text> texts = new ArrayList<>();

                    for(WTPokemon pokemon : PixelmonCommands.getInstance().WTPool) {
                        texts.add(buildPokemonStats(pokemon));
                    }

                    PaginationList.builder()
                            .contents(texts)
                            .padding(Text.of(""))
                            .title(Text.of(Chat.embedColours("&7WonderTrade Pool")))
                            .build()
                            .sendTo(src);
                } else if(type.get().equals("add")) {
                    if(slot.isPresent()) {
                        if (slot.get() < 1 || slot.get() > 6) throw new CommandException(Text.of(TextColors.RED,"Invalid slot #... must be between 1 and 6."));

                        PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) src);
                        if (storage != null) {
                            Pokemon pokemon = storage.get(slot.get() - 1);

                            List<Integer> ids = Lists.newArrayList();
                            PixelmonCommands.getInstance().getWTPool().forEach(p -> ids.add(p.getId()));

                            NBTTagCompound nbt = new NBTTagCompound();
                            pokemon.writeToNBT(nbt);

                            PixelmonCommands.getInstance().WTPool.add(new WTPokemon(Collections.max(ids) + 1, nbt.toString()));
                            storage.set(slot.get() - 1, null);

                            if (MainConfig.enableMessages) {
                                if (pokemon.isShiny() || EnumSpecies.legendaries.contains(pokemon.getDisplayName())) {
                                    if (!pokemon.isEgg()) {
                                        Chat.sendBroadcastMessage(src, MainConfig.getMessages("Messages.WT.LegendaryShinyAdded")
                                                .replace("{player}", src.getName())
                                                .replace("{type}", pokemon.isShiny() ? "Shiny" : "Legendary")
                                        );
                                    }
                                }
                            }
                        }
                    } else {
                        Chat.sendMessage(src, "&7You need to enter a slot # first.");
                    }
                } else if(type.get().equals("generate")) {
                    PixelmonCommands.getInstance().WTPool.clear();

                    for(int i = 1; i <= MainConfig.maxWTPool; ++i) {
                        com.pixelmonmod.pixelmon.api.pokemon.Pokemon p = PixelmonUtils.getRandomEntityPixelmon();
                        if (p != null) {
                            PixelmonCommands.getInstance().WTPool.add(new WTPokemon(i, PixelmonUtils.getNbt(p).toString()));
                        }
                    }

                    Chat.sendServerWideMessage(MainConfig.getMessages("Messages.WT.GenerateNewPool"));
                }
            }

            return CommandResult.success();
        }
    }

    public static class WTPool implements CommandExecutor {
        @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

            List<WTPokemon> pokemon = PixelmonCommands.getInstance().WTPool;

            long legendaries = pokemon.stream().filter(p -> EnumSpecies.legendaries.contains(p.getPokemon().getSpecies().name) || EnumSpecies.ultrabeasts.contains(p.getPokemon().getSpecies().name)).count();
            long shiny = pokemon.stream().filter(p -> p.getPokemon().isShiny()).count();

            Chat.sendMessage(src, "&bThere are currently &a" + shiny + " shiny Pok\u00E9mon &band &a" + legendaries + " legendary Pok\u00E9mon &bin WonderTrade.");

            return CommandResult.success();
        }
    }

    private static Text buildPokemonStats(WTPokemon poke) {
        Pokemon pokemon = poke.getPokemon();
        String displayName = poke.getId() + ": " + PixelmonUtils.updatePokemonName(pokemon.getSpecies().name());
        String pokerus = pokemon.getPokerus() != null ? (pokemon.getPokerus().canInfect() ? "&d[PKRS] " : "&7&m[PKRS] ") : "";
        boolean isTrio = false;
        String heldItem;
        Stats stats = pokemon.getStats();
        Gender gender = pokemon.getGender();
        EVStore eVsStore = null;
        IVStore ivStore = null;
        EnumNature nature = pokemon.getNature();
        boolean wasHyperTrained = false;
        String[] ht = new String[]{"","","","","",""};

        String formName = "";
        if (!(pokemon.getFormEnum().equals(EnumSpecial.Base) || pokemon.getFormEnum().getLocalizedName().equals("None") || pokemon.getFormEnum().getLocalizedName().equals("Standard") || pokemon.getFormEnum().getLocalizedName().equals("Normal"))) {
            formName = pokemon.getFormEnum().getLocalizedName();
        }

        boolean isEgg = pokemon.isEgg();
        String eggDetails = "";

        NBTTagCompound nbt = new NBTTagCompound();
        pokemon.writeToNBT(nbt);

        switch (pokemon.getSpecies()) {
            case Mesprit:
            case Azelf:
            case Uxie:
                isTrio = true;
        }

        boolean inRanch = pokemon.isInRanch();
        Boolean isShiny = pokemon.isShiny();
        Moveset moveset = pokemon.getMoveset();
        heldItem = !pokemon.getHeldItem().getDisplayName().equalsIgnoreCase("Air") ? pokemon.getHeldItem().getDisplayName() : "Nothing";

        TextColor nameColor = (isShiny) ? TextColors.GOLD : TextColors.DARK_AQUA;

        String pokeName;
        if(EnumSpecies.legendaries.contains(displayName)) {
            nameColor = TextColors.LIGHT_PURPLE;
            pokeName = "&d" + displayName;
        } else{
            pokeName = "&3" + displayName;
        }

        int ivSum = 0;
        int evSum = 0;
        if (stats != null) {
            eVsStore = stats.evs;
            ivStore = stats.ivs;

            ivSum = ivStore.getStat(StatsType.HP) + ivStore.getStat(StatsType.Attack) + ivStore.getStat(StatsType.Defence) + ivStore.getStat(StatsType.SpecialAttack) + ivStore.getStat(StatsType.SpecialDefence) + ivStore.getStat(StatsType.Speed);
            evSum = eVsStore.getStat(StatsType.HP) + eVsStore.getStat(StatsType.Attack) + eVsStore.getStat(StatsType.Defence) + eVsStore.getStat(StatsType.SpecialAttack) + eVsStore.getStat(StatsType.SpecialDefence) + eVsStore.getStat(StatsType.Speed);

            StatsType[] stat = new StatsType[]{StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed};

            for(int i = 0; i < stat.length; ++i) {
                if (ivStore.isHyperTrained(stat[i])) {
                    ht[i] = "&3";
                    wasHyperTrained = true;
                }
            }
        }

        String pokeGender;
        if (gender.toString().equals("Female")) pokeGender = "&d" + gender.toString() + " \u2640";
        else if(gender.toString().equals("Male")) pokeGender = "&b" + gender.toString() + " \u2642";
        else pokeGender = "&8Genderless \u26A5";

        ArrayList<String> moves = new ArrayList<>();
        moves.add((moveset.get(0)==null) ? "&bNone" : "&b"+moveset.get(0).getActualMove().getLocalizedName());
        moves.add((moveset.get(1)==null) ? "&bNone" : "&b"+moveset.get(1).getActualMove().getLocalizedName());
        moves.add((moveset.get(2)==null) ? "&bNone" : "&b"+moveset.get(2).getActualMove().getLocalizedName());
        moves.add((moveset.get(3)==null) ? "&bNone" : "&b"+moveset.get(3).getActualMove().getLocalizedName());

        String Aura = "";
        if (pokemon.getPersistentData().hasKey("HasAura")) {
            AuraDefinition ad = FileHelper.getAuraDefinitionForID(pokemon.getPersistentData().getInteger("AuraType"));
            EffectDefinition ed = FileHelper.getEffectDefinitionForID(pokemon.getPersistentData().getInteger("AuraEffect"));
            Aura = ad.getDisplayName() + " " + ed.getDisplayName();
        }

        DecimalFormat df = new DecimalFormat("#0.##");
        int numEnchants = 0;
        try {
            if (pokemon.getExtraStats() != null && pokemon.getExtraStats() instanceof LakeTrioStats) {
                LakeTrioStats extra = (LakeTrioStats)pokemon.getExtraStats();
                numEnchants = PixelmonConfig.lakeTrioMaxEnchants - extra.numEnchanted;
            }
        }
        catch (Exception extra) {
            // empty catch block
        }
        String pokeStats = pokerus + pokeName + " &7| &eLvl " + pokemon.getLevel() + (pokemon.getDynamaxLevel() != pokemon.getLevel() ? "&7(&3" + pokemon.getDynamaxLevel() + "&7)" : "") + " " + ((isShiny) ? "&7(&6Shiny&7)&r " : "") + "\n&r" +
                (new PokemonSpec("untradeable").matches(pokemon) ? "&4Untradeable" + "\n&r" : "") +
                (!Strings.isNullOrEmpty(Aura) ? "&7Aura: " + Aura + "\n&r" : "") +
                (pokemon.hasGigantamaxFactor() ? "&7G-Max Potential" + "\n&r": "") +
                (!formName.isEmpty() ? "&7Form: &e" + WordUtils.capitalizeFully(formName) + "\n&r" : "") +
                (isTrio ? "&7Ruby Enchant: &e" + (numEnchants != 0 ? numEnchants + " Available" : "None Available") + "\n&r" : "") +
                (!pokemon.getHeldItem().getDisplayName().equalsIgnoreCase("Air") ? "&7Held Item: &e" + heldItem + "\n&r" : "") +
                "&7Ability: &e" + pokemon.getAbility().getName() + ((PixelmonUtils.isHiddenAbility(pokemon,pokemon.getAbility().getName())) ? " &7(&6HA&7)&r" : "") + "\n&r" +
                "&7Nature: &e" + nature.toString() + " &7(&a+" + PixelmonUtils.getNatureShorthand(nature.increasedStat) + " &7| &c-" + PixelmonUtils.getNatureShorthand(nature.decreasedStat) + "&7)" + "\n&r" +
                "&7Gender: " + pokeGender + "\n&r" +
                "&7Size: &e" + pokemon.getGrowth().toString() + "\n&r" +
                "&7Happiness: &e" + pokemon.getFriendship() + "\n&r" +
                "&7Hidden Power: &e" + HiddenPower.getHiddenPowerType(pokemon.getIVs()).getLocalizedName() + "\n&r" +
                "&7Caught Ball: &e" + pokemon.getCaughtBall().getItem().getLocalizedName() + "\n\n&r" +

                "&7IVs: &e" + ivSum + "&7/&e186 &7(&a" + df.format((int)(((double)ivSum/186)*100)) + "%&7) \n"
                + "&cHP: " + ht[0] + ivStore.getStat(StatsType.HP) + " &7/ "
                + "&6Atk: " + ht[1] + ivStore.getStat(StatsType.Attack) + " &7/ "
                + "&eDef: " + ht[2] + ivStore.getStat(StatsType.Defence) + "\n"
                + "&9SpA: " + ht[3] + ivStore.getStat(StatsType.SpecialAttack) + " &7/ "
                + "&aSpD: " + ht[4] + ivStore.getStat(StatsType.SpecialDefence) + " &7/ "
                + "&dSpe: " + ht[5] + ivStore.getStat(StatsType.Speed) + "\n" +
                "&7EVs: &e" + evSum + "&7/&e510 &7(&a" + df.format((int)(((double)evSum/510)*100)) + "%&7) \n"
                + "&cHP: " + eVsStore.getStat(StatsType.HP) + " &7/ "
                + "&6Atk: " + eVsStore.getStat(StatsType.Attack) + " &7/ "
                + "&eDef: " + eVsStore.getStat(StatsType.Defence) + "\n"
                + "&9SpA: " + eVsStore.getStat(StatsType.SpecialAttack) + " &7/ "
                + "&aSpD: " + eVsStore.getStat(StatsType.SpecialDefence) + " &7/ "
                + "&dSpe: " + eVsStore.getStat(StatsType.Speed) + "\n\n" +

                "&7Moves:\n" + moves.get(0) + " &7- " + moves.get(1) + " &7- " + moves.get(2) + " &7- " + moves.get(3);
        if(!isEgg) {
            return Text.builder().color(nameColor)
                    .append(Text.of(Chat.embedColours("[" + displayName + "]")))
                    .onHover(TextActions.showText(Text.of(Chat.embedColours(pokeStats))
                    )).onClick(TextActions.executeCallback(s -> givePokemon(s, pokemon, poke.getId()))).build();
        }else if(inRanch) {
            return Text.builder().color(nameColor)
                    .append(Text.of(Chat.embedColours("&c[Bugged WTPokemon]")))
                    .onHover(TextActions.showText(Text.of(Chat.embedColours("&7This WTPokemon is stuck in a ranch block... help it out!"))
                    )).build();
        }else {
            return Text.builder().color(nameColor)
                    .append(Text.of(Chat.embedColours("[Mystery Egg]")))
                    .onHover(TextActions.showText(Text.of(Chat.embedColours(eggDetails))
                    )).build();
        }
    }

    private static void givePokemon(CommandSource src, Pokemon pokemon, int id) {
        if(src.hasPermission("pixelcommands.admin.wt.collect")) {
            EntityPlayerMP player = (EntityPlayerMP) src;
            PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage(player);

            if (storage != null) {
                Pixelmon.EVENT_BUS.post(new PixelmonReceivedEvent(player, ReceiveType.Command, pokemon));
                storage.add(pokemon);

                PixelmonCommands.getInstance().WTPool.removeIf(p -> p.getId() == id);

                Pokemon p = PixelmonUtils.getRandomEntityPixelmon();
                if (p != null && PixelmonCommands.getInstance().WTPool.size() < MainConfig.maxWTPool) {
                    PixelmonCommands.getInstance().WTPool.add(new WTPokemon(id, PixelmonUtils.getNbt(p).toString()));
                }
            }
        }
    }
}
