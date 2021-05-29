package com.m1zark.pixelmoncommands.UI;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.m1utilities.api.GUI.InventoryManager;
import com.m1zark.pixelmoncommands.WT.PokemonData;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;

public class CheckPartyUI extends InventoryManager {
    private PlayerPartyStorage storage;

    public CheckPartyUI(Player p) {
        super(p, 3, Text.of(Chat.embedColours("&4&lPok\u00E9mon Stats")));
        this.player = p;
        this.storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) p);

        this.setupDesign();
    }

    private void setupDesign() {
        int s = 0;

        for(int x = 0; x < 9; x++) { this.addIcon(BorderIcon(x, DyeColors.RED, "")); }

        this.addIcon(BorderIcon(9, DyeColors.BLACK, ""));

        for (int slot = 10; slot <= 16; slot++) {
            if (slot == 13) {
                this.addIcon(dividerIcon(slot));
            } else {
                com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon = storage.get(s);
                PokemonData pkmn = new PokemonData(pokemon);

                if (pokemon != null) {
                    Icon poke = new Icon(slot, pkmn.getSprite("stats", this.player));
                    this.addIcon(poke);
                } else {
                    this.addIcon(BorderIcon(slot, DyeColors.GRAY, "Slot " + (s + 1) + " is empty!"));
                }

                s++;
            }
        }

        this.addIcon(BorderIcon(17, DyeColors.BLACK, ""));

        for(int x = 18; x < 27; x++) { this.addIcon(BorderIcon(x, DyeColors.WHITE, "")); }
    }

    private static Icon BorderIcon(int slot, DyeColor color, String name) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours(name)))
                .add(Keys.DYE_COLOR, color)
                .build());
    }

    private static Icon dividerIcon(int slot) {
        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_panel").orElse(ItemTypes.BARRIER))
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("")))
                .add(Keys.ITEM_LORE, new ArrayList<>())
                .build());
    }
}
