package com.m1zark.pixelmoncommands.WT;

import com.google.common.base.Strings;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.client.gui.GuiResources;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import de.waterdu.aquaauras.auras.AuraStorage;
import de.waterdu.aquaauras.helper.FileHelper;
import de.waterdu.aquaauras.structures.AuraDefinition;
import de.waterdu.aquaauras.structures.EffectDefinition;
import net.minecraft.client.gui.Gui;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PokemonData {
    private Pokemon pokemon;

    private int id;
    private int form;
    private String formName = "";
    private String name;
    private String ability;
    private String m1;
    private String m2;
    private String m3;
    private String m4;
    private int lvl;
    private String growth;
    private String gender;
    private boolean shiny;
    private String nature;
    private int happiness;
    private String caughtBall;
    private boolean isEgg;
    private boolean isHiddenAbility;
    private String heldItem = "Nothing";
    private String hiddenPower;
    private List<String> PokemonAuras = new ArrayList<>();
    private int AurasEnabled = 0;
    private String specialTexture;
    private String pokerus;
    private boolean isTrio = false;
    private int numEnchanted;
    private String customTexture;

    private int totalIVs;
    private String IVPercent;
    private int[] ivs = new int[]{0, 0, 0, 0, 0, 0};

    private int totalEVs;
    private String EVPercent;
    private int evs[] = new int[]{0, 0, 0, 0, 0, 0};

    private boolean wasHyperTrained = false;
    private String[] ht = new String[]{"", "", "", "", "", ""};

    public PokemonData(Pokemon pokemon) {
        DecimalFormat df = new DecimalFormat("##0");

        if(pokemon != null) {
            this.pokemon = pokemon;
            this.id = pokemon.getSpecies().getNationalPokedexInteger();
            this.form = pokemon.getForm();

            if (!(pokemon.getFormEnum().equals(EnumSpecial.Base) || pokemon.getFormEnum().getLocalizedName().equals("None") || pokemon.getFormEnum().getLocalizedName().equals("Standard") || pokemon.getFormEnum().getLocalizedName().equals("Normal"))) {
                this.formName = pokemon.getFormEnum().getLocalizedName();
            }

            switch (pokemon.getSpecies()) {
                case Mesprit:
                case Azelf:
                case Uxie:
                    isTrio = true;
                    try {
                        if (pokemon.getExtraStats() != null && pokemon.getExtraStats() instanceof LakeTrioStats) {
                            LakeTrioStats extra = (LakeTrioStats) pokemon.getExtraStats();
                            this.numEnchanted = PixelmonConfig.lakeTrioMaxEnchants - extra.numEnchanted;
                        }
                    } catch (Exception extra) {

                    }
            }

            this.customTexture = pokemon.getCustomTexture();
            this.name = pokemon.getSpecies().name();
            this.ability = pokemon.getAbility().getName();
            this.pokerus = pokemon.getPokerus() != null ? (pokemon.getPokerus().canInfect() ? "&d[PKRS] " : "&7&m[PKRS] ") : "";
            this.isHiddenAbility = PixelmonUtils.isHiddenAbility(pokemon, this.ability);
            this.heldItem = !pokemon.getHeldItem().getDisplayName().equalsIgnoreCase("Air") ? pokemon.getHeldItem().getDisplayName() : "Nothing";
            this.m1 = pokemon.getMoveset().get(0) != null ? pokemon.getMoveset().get(0).getActualMove().getLocalizedName() : "Empty";
            this.m2 = pokemon.getMoveset().get(1) != null ? pokemon.getMoveset().get(1).getActualMove().getLocalizedName() : "Empty";
            this.m3 = pokemon.getMoveset().get(2) != null ? pokemon.getMoveset().get(2).getActualMove().getLocalizedName() : "Empty";
            this.m4 = pokemon.getMoveset().get(3) != null ? pokemon.getMoveset().get(3).getActualMove().getLocalizedName() : "Empty";
            this.isEgg = pokemon.isEgg();
            this.lvl = pokemon.getLevel();
            this.nature = pokemon.getNature().name();
            this.gender = pokemon.getGender().name();
            this.shiny = pokemon.isShiny();
            this.growth = pokemon.getGrowth().name();
            this.happiness = pokemon.getFriendship();
            this.caughtBall = pokemon.getCaughtBall().name();

            this.ivs[0] = pokemon.getIVs().getStat(StatsType.HP);
            this.ivs[1] = pokemon.getIVs().getStat(StatsType.Attack);
            this.ivs[2] = pokemon.getIVs().getStat(StatsType.Defence);
            this.ivs[3] = pokemon.getIVs().getStat(StatsType.SpecialAttack);
            this.ivs[4] = pokemon.getIVs().getStat(StatsType.SpecialDefence);
            this.ivs[5] = pokemon.getIVs().getStat(StatsType.Speed);
            this.totalIVs = this.ivs[0] + this.ivs[1] + this.ivs[2] + this.ivs[3] + this.ivs[4] + this.ivs[5];
            this.IVPercent = df.format((double) totalIVs / 186.0D * 100.0D) + "%";
            StatsType[] stat = new StatsType[]{StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed};
            for (int i = 0; i < stat.length; ++i) {
                if (!pokemon.getIVs().isHyperTrained(stat[i])) continue;
                this.ht[i] = "&3";
                this.wasHyperTrained = true;
            }

            this.evs[0] = pokemon.getEVs().getStat(StatsType.HP);
            this.evs[1] = pokemon.getEVs().getStat(StatsType.Attack);
            this.evs[2] = pokemon.getEVs().getStat(StatsType.Defence);
            this.evs[3] = pokemon.getEVs().getStat(StatsType.SpecialAttack);
            this.evs[4] = pokemon.getEVs().getStat(StatsType.SpecialDefence);
            this.evs[5] = pokemon.getEVs().getStat(StatsType.Speed);
            this.totalEVs = this.evs[0] + this.evs[1] + this.evs[2] + this.evs[3] + this.evs[4] + this.evs[5];
            this.EVPercent = df.format((double) totalEVs / 510.0D * 100.0D) + "%";
            this.hiddenPower = HiddenPower.getHiddenPowerType(pokemon.getIVs()).getLocalizedName();

            AuraStorage auras = new AuraStorage(pokemon.getPersistentData());
            if(auras.hasAuras()) {
                auras.getAuras().forEach(aura -> {
                    if(aura.isEnabled()) this.PokemonAuras.add(aura.getAuraDefinition().getDisplayName() + " " + aura.getEffectDefinition().getDisplayName());
                });
            }
            this.AurasEnabled = auras.aurasEnabled();
        }else{
            //PixelEdit.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PEInfo.DEBUG_PREFIX, "Missing Pixelmon Data!")));
        }
    }

    public boolean isEgg() { return this.isEgg; }

    public ItemStack getSprite(String title, Player p) {
        Optional<ItemType> sprite = Sponge.getRegistry().getType(ItemType.class, "pixelmon:pixelmon_sprite");
        ItemStack Item = ItemStack.builder().itemType(sprite.get()).build();

        ItemStack item = this.setPicture(Item);
        this.setItemData(item, title);
        return item;
    }

    private ItemStack setPicture(ItemStack item) {
        String spriteData;

        if(pokemon.isEgg()) {
            spriteData = "pixelmon:" + GuiResources.getEggSprite(pokemon.getSpecies(), pokemon.getEggCycles());
        }else {
            spriteData = "pixelmon:" + GuiResources.getSpritePath(pokemon.getSpecies(), pokemon.getForm(), pokemon.getGender(), pokemon.getCustomTexture(), pokemon.isShiny());
        }

        return ItemStack.builder().fromContainer(item.toContainer().set(DataQuery.of("UnsafeData","SpriteName"), spriteData)).build();
    }

    private void setItemData(ItemStack item, String title) {
        String displayName = this.pokerus + "&b"+PixelmonUtils.updatePokemonName(this.name)+" &7| &eLvl "+this.lvl+" "+((this.shiny) ? "&7(&6Shiny&7)&r" : "");
        String pokeGender = (this.gender.equals("Male")) ? "&b" + this.gender : (this.gender.equals("Female")) ? "&d" + this.gender : "Genderless";

        if(this.isEgg){
            item.offer(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&7Pok\u00E9mon Egg")));
        }else{
            item.offer(Keys.DISPLAY_NAME, Text.of(Chat.embedColours(displayName)));
        }

        ArrayList<Text> itemLore = new ArrayList<>();
        if(!this.isEgg) {
            //if (!title.equals("")) itemLore.add(Text.of(Chat.embedColours(displayName)));
            if (new PokemonSpec("unbreedable").matches(this.pokemon)) itemLore.add(Text.of(Chat.embedColours("&4Unbreedable")));
            if (!title.equals("") && !PokemonAuras.isEmpty()) itemLore.add(Text.of(Chat.embedColours("&7Aura(s): " + this.PokemonAuras.get(0) + (this.AurasEnabled > 1 ? " + " + this.PokemonAuras.get(1) : ""))));
            if (!title.equals("") && pokemon.hasGigantamaxFactor()) itemLore.add(Text.of(Chat.embedColours("&7G-Max Potential")));
            if (!title.equals("") && !formName.isEmpty()) itemLore.add(Text.of(Chat.embedColours("&7Form: &e" + PixelmonUtils.capitalize(this.formName))));
            if (!title.equals("") && isTrio) itemLore.add(Text.of(Chat.embedColours("&7Ruby Enchant: &e" + (this.numEnchanted != 0 ? this.numEnchanted + " Available" : "None Available"))));
            if (!title.equals("") && !Strings.isNullOrEmpty(customTexture)) itemLore.add(Text.of(Chat.embedColours("&7Custom Texture: &e" + PixelmonUtils.capitalize(this.customTexture))));
            itemLore.add(Text.of(Chat.embedColours("&7Ability: &e" + this.ability + (this.isHiddenAbility ? " &7(&6HA&7)&r" : ""))));
            itemLore.add(Text.of(Chat.embedColours("&7Nature: &e" + this.nature)));
            itemLore.add(Text.of(Chat.embedColours("&7Gender: " + pokeGender)));
            itemLore.add(Text.of(Chat.embedColours("&7Size: &e" + this.growth)));
            if (!title.equals("")) itemLore.add(Text.of(Chat.embedColours("&7Happiness: &e" + this.happiness)));
            if (!title.equals("")) itemLore.add(Text.of(Chat.embedColours("&7Hidden Power: &e" + this.hiddenPower)));

            itemLore.add(Text.of(Chat.embedColours("")));
            if (!heldItem.equals("Nothing")) itemLore.add(Text.of(Chat.embedColours("&7Held Item: &e" + this.heldItem)));
            itemLore.add(Text.of(Chat.embedColours("&7CaughtBall: &e" + this.caughtBall)));
            if (!title.equals("")) itemLore.add(Text.of(Chat.embedColours("")));
            itemLore.add(Text.of(Chat.embedColours("&7IVs: &e" + this.totalIVs + "&7/&e186 &7(&a" + this.IVPercent + "&7)")));
            if (!title.equals("")) itemLore.add(Text.of(Chat.embedColours("&cHP: " + this.ht[0] + this.ivs[0] + "&7/&6Atk: " + this.ht[1] + this.ivs[1] + "&7/&eDef: " + this.ht[2] + this.ivs[2] + "&7/&9SpA: " + this.ht[3] + this.ivs[3] + "&7/&aSpD: " + this.ht[4] + this.ivs[4] + "&7/&dSpe: " + this.ht[5] + this.ivs[5])));
            if (!title.equals("")) itemLore.add(Text.of(Chat.embedColours("")));
            itemLore.add(Text.of(Chat.embedColours("&7EVs: &e" + this.totalEVs + "&7/&e510 &7(&a" + this.EVPercent + "&7)")));
            if (!title.equals("")) itemLore.add(Text.of(Chat.embedColours("&cHP: " + this.evs[0] + "&7/&6Atk: " + this.evs[1] + "&7/&eDef: " + this.evs[2] + "&7/&9SpA: " + this.evs[3] + "&7/&aSpD: " + this.evs[4] + "&7/&dSpe: " + this.evs[5])));
            if (!title.equals("")) {
                itemLore.add(Text.of(Chat.embedColours("")));
                itemLore.add(Text.of(Chat.embedColours("&7Moves:")));
                itemLore.add(Text.of(Chat.embedColours("&b" + this.m1 + " &7- &b" + this.m2)));
                itemLore.add(Text.of(Chat.embedColours("&b" + this.m3 + " &7- &b" + this.m4)));
            }
        }else{
            itemLore.add(Text.of(Chat.embedColours("Maybe wait til it hatches first...")));
        }

        item.offer(Keys.ITEM_LORE, itemLore);
    }
}
