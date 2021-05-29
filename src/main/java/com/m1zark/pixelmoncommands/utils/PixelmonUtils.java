package com.m1zark.pixelmoncommands.utils;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.WT.WTPokemon;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.client.gui.GuiResources;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.*;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import net.minecraft.item.Item;

import javax.annotation.Nullable;
import java.util.*;

import com.pixelmonmod.pixelmon.config.PixelmonItemsTMs;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen1TechnicalMachines;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen2TechnicalMachines;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen3TechnicalMachines;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen4TechnicalMachines;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen5TechnicalMachines;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen6TechnicalMachines;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen7TechnicalMachines;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen8TechnicalMachines;
import com.pixelmonmod.pixelmon.enums.technicalmoves.Gen8TechnicalRecords;
import com.pixelmonmod.pixelmon.enums.technicalmoves.ITechnicalMove;

public class PixelmonUtils {
    private static final Random RANDOM = new Random();
    public static ArrayList<Item> tms = new ArrayList();
    public static ArrayList<Item> galartms = new ArrayList();
    public static ArrayList<Item> trs = new ArrayList();


    @Nullable
    public static PlayerPartyStorage getPlayerStorage(EntityPlayerMP player) {
        return Pixelmon.storageManager.getParty(player);
    }

    public static void generateRandomPool(Integer intSize) {
        for(int i = 1; i <= intSize; ++i) {
            Pokemon p = getRandomEntityPixelmon();
            if (p != null) {
                PixelmonCommands.getInstance().getSql().addPokemon(new WTPokemon(i, PixelmonUtils.getNbt(p).toString()));
            }
        }
    }

    public static Pokemon getRandomEntityPixelmon() {
        Pokemon pokemon;
        if (RandomHelper.getRandomNumberBetween(1, 8000) == 1) {
            String legendary;
            do {
                legendary = EnumSpecies.legendaries.get(RandomHelper.getRandomNumberBetween(0, EnumSpecies.legendaries.size() - 1));
            } while(!EnumSpecies.getNameList().contains(legendary));

            pokemon = Pixelmon.pokemonFactory.create(EnumSpecies.getFromNameAnyCase(legendary));
        } else {
            do {
                pokemon = Pixelmon.pokemonFactory.create(EnumSpecies.randomPoke());
            } while(EnumSpecies.legendaries.contains(pokemon.getSpecies().name));
        }

        pokemon.setLevel(RandomHelper.getRandomNumberBetween(1, PixelmonConfig.maxLevelByDistance));
        pokemon.setShiny(RandomHelper.getRandomNumberBetween(1, (int)PixelmonConfig.shinyRate) <= 1 && PixelmonConfig.shinyRate != 0.0F);
        pokemon.setCaughtBall(EnumPokeballs.PokeBall);

        return pokemon;
    }

    public static NBTTagCompound getNbt(Pokemon pokemon) {
        NBTTagCompound nbt = new NBTTagCompound();
        pokemon.writeToNBT(nbt);

        return nbt;
    }

    public static Pokemon getPokemon(String nbt) {
        try {
            return Pixelmon.pokemonFactory.create(JsonToNBT.getTagFromJson(nbt));
        } catch(NBTException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack disguiseItem(boolean shiny, String pokemon) {
        int dexNumber = Pixelmon.pokemonFactory.create(EnumSpecies.getFromNameAnyCase(pokemon)).getBaseStats().nationalPokedexNumber;
        String idValue = String.format("%03d", dexNumber);
        String spriteData;

        //if(shiny) { spriteData = "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon, 0); }
        //else { spriteData = "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon, 0); }
        spriteData = "pixelmon:" + GuiResources.getSpritePath(EnumSpecies.getFromNameAnyCase(pokemon), 0, Gender.Male, false, shiny);

        Optional<ItemType> sprite = Sponge.getRegistry().getType(ItemType.class, "pixelmon:pixelmon_sprite");
        ItemStack Item = ItemStack.builder().itemType(sprite.get()).build();

        Item.offer(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&f" + (shiny ? "Shiny " : "") + WordUtils.capitalizeFully(pokemon) + " Disguise")));
        Item.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(Chat.embedColours("&aRight click to unlock the &b" + (shiny ? "Shiny " : "") + WordUtils.capitalizeFully(pokemon) + " Disguise&a!"))));

        Item.offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(Enchantment.of(EnchantmentTypes.UNBREAKING, 1)));
        Item.offer(Keys.HIDE_ENCHANTMENTS, true);

        return ItemStack.builder().fromContainer(Item.toContainer().set(DataQuery.of("UnsafeData","SpriteName"), spriteData).set(DataQuery.of("UnsafeData", "disguise"), 1)).build();
    }

    public static ItemStack xpItem(String type, int time) {
        ItemStack Item2 = ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "minecraft:speckled_melon").orElse(ItemTypes.BARRIER)).build();

        String h = time == 1 ? time + " hour" : time + " hours";
        String t = type.equals("player") ? "receive" : "give the whole server";

        Item2.offer(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&aDouble EXP Melon &7(&b" + h + "&7)")));
        Item2.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(Chat.embedColours("&7Right click to " + t + " &b" + h + " &7of double exp gain!"))));

        Item2.offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(Enchantment.of(EnchantmentTypes.UNBREAKING, 1)));
        Item2.offer(Keys.HIDE_ENCHANTMENTS, true);

        return ItemStack.builder().fromContainer(Item2.toContainer().set(DataQuery.of("UnsafeData", "xp"), type + ":" + time + "h")).build();
    }

    public static boolean isHiddenAbility(Pokemon p, String ability) {
        return p.getAbilitySlot() == 2;
    }

    public static String updatePokemonName(String name){
        if(name.equalsIgnoreCase("MrMime")) return "Mr. Mime";
        else if(name.equalsIgnoreCase("MimeJr")) return "Mime Jr.";
        else if(name.equalsIgnoreCase("Nidoranfemale")) return "Nidoran&d\u2640&r";
        else if(name.equalsIgnoreCase("Nidoranmale")) return "Nidoran&b\u2642&r";
        else if(name.equalsIgnoreCase("Farfetchd")) return "Farfetch'd";
        else if(name.contains("Alolan")){
            return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(name.replaceAll("\\d+", "")), " ");
        }

        return name;
    }

    public static String getNatureShorthand(StatsType type) {
        switch (type) {
            case Accuracy: {
                return "Acc";
            }
            case HP: {
                return "HP";
            }
            case Speed: {
                return "Speed";
            }
            case Attack: {
                return "Atk";
            }
            case Defence: {
                return "Def";
            }
            case Evasion: {
                return "Eva";
            }
            case SpecialAttack: {
                return "SpAtk";
            }
            case SpecialDefence: {
                return "SpDef";
            }
            case None: {
                return "None";
            }
        }
        return "";
    }

    public static String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static boolean between(int i, int minValueInclusive, int maxValueInclusive) {
        if (i >= minValueInclusive && i <= maxValueInclusive)
            return true;
        else
            return false;
    }


    public static void loadLists() {
        addMachineItemsToList((ITechnicalMove[])Gen1TechnicalMachines.values(), tms);
        addMachineItemsToList((ITechnicalMove[])Gen2TechnicalMachines.values(), tms);
        addMachineItemsToList((ITechnicalMove[])Gen3TechnicalMachines.values(), tms);
        addMachineItemsToList((ITechnicalMove[])Gen4TechnicalMachines.values(), tms);
        addMachineItemsToList((ITechnicalMove[])Gen5TechnicalMachines.values(), tms);
        addMachineItemsToList((ITechnicalMove[])Gen6TechnicalMachines.values(), tms);
        addMachineItemsToList((ITechnicalMove[])Gen7TechnicalMachines.values(), tms);
        addMachineItemsToList((ITechnicalMove[])Gen8TechnicalMachines.values(), galartms);
        addMachineItemsToList((ITechnicalMove[])Gen8TechnicalRecords.values(), trs);
        tms.addAll(PixelmonItemsTMs.HMs);
    }

    private static void addMachineItemsToList(ITechnicalMove[] tmEnum, List<Item> list) {
        for (ITechnicalMove enumValue : tmEnum) {
            list.add(PixelmonItemsTMs.createStackFor((ITechnicalMove)enumValue).getItem());
        }
    }

    public static net.minecraft.item.ItemStack randomTM() {
        loadLists();

        Item tm = tms.get(new Random().nextInt(tms.size()));
        return tm.getDefaultInstance();
    }

    public static net.minecraft.item.ItemStack randomGalarTM() {
        Item galartm = galartms.get(new Random().nextInt(galartms.size()));
        return galartm.getDefaultInstance();
    }

    public static net.minecraft.item.ItemStack randomTR() {
        Item tr = trs.get(new Random().nextInt(trs.size()));
        return tr.getDefaultInstance();
    }
}
