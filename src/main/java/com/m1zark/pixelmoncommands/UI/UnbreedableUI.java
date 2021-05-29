package com.m1zark.pixelmoncommands.UI;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.m1utilities.api.GUI.InventoryManager;
import com.m1zark.m1utilities.api.Money;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.client.gui.GuiResources;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;

public class UnbreedableUI extends InventoryManager {
    private Player player;
    private PlayerPartyStorage storage;
    private boolean confirm = false;
    private int slot;
    private static final PokemonSpec UNBREEDABLE = new PokemonSpec("unbreedable");

    public UnbreedableUI(Player p) {
        super(p, 3, Text.of(Chat.embedColours("&4&lPok\u00E9mon Alterations")));
        this.player = p;
        this.storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) p);

        this.setupDesign();
    }

    public UnbreedableUI(Player p, boolean confirm, int slot) {
        super(p, 3, Text.of(Chat.embedColours("&4&lConfirm Alterations")));
        this.player = p;
        this.storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) p);
        this.confirm = confirm;
        this.slot = slot;

        this.setupDesign();
    }

    private void setupDesign() {
        int s = 0;

        for(int x = 0; x < 9; x++) { this.addIcon(BorderIcon(x, DyeColors.RED, "")); }

        this.addIcon(BorderIcon(9, DyeColors.BLACK, ""));

        if(this.confirm) {
            Pokemon pokemon = storage.get(this.slot);

            Icon confirmIcon = confirmIcon(11);
            confirmIcon.addListener(clickable ->  {
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
                        if (!UNBREEDABLE.matches(pokemon) && !pokemon.isEgg()) {
                            if (Money.canPay(player, MainConfig.getUnbreedableCost()) && Money.withdrawn(player, MainConfig.getUnbreedableCost())) {
                                UNBREEDABLE.apply(pokemon);
                                Chat.sendMessage(player, "&7Your &b" + pokemon.getSpecies().name + " &7is now marked as unbreedable.");
                            } else {
                                Chat.sendMessage(player, "&7You do not have enough money todo this.");
                            }

                            this.player.closeInventory();
                        }
                    } else if (clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {

                    }
                }).delayTicks(1L).submit(PixelmonCommands.getInstance());
            });
            this.addIcon(confirmIcon);

            this.addIcon(pokemonIcon(13, pokemon, this.slot+1));

            Icon close = closeIcon(15);
            close.addListener(clickable ->  {
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    this.player.openInventory((new UnbreedableUI(this.player)).getInventory());
                }).delayTicks(1L).submit(PixelmonCommands.getInstance());
            });
            this.addIcon(close);
        } else {
            for (int slot = 10; slot <= 16; slot++) {
                if (slot == 13) {
                    this.addIcon(dividerIcon(slot));
                } else {
                    Pokemon pokemon = storage.get(s);

                    final int sl = s;

                    if (pokemon != null) {
                        Icon poke = pokemonIcon(slot, pokemon, sl + 1);
                        poke.addListener(clickable -> {
                            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
                                    if (!UNBREEDABLE.matches(pokemon) && !pokemon.isEgg()) {
                                        this.player.openInventory((new UnbreedableUI(this.player, true, sl)).getInventory());
                                    }
                                } else if (clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {

                                }
                            }).delayTicks(1L).submit(PixelmonCommands.getInstance());
                        });
                        this.addIcon(poke);
                    } else {
                        this.addIcon(BorderIcon(sl, DyeColors.GRAY, "Slot " + (sl + 1) + " is empty!"));
                    }

                    s++;
                }
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

    private static Icon confirmIcon(int slot) {
        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&7Clicking this button will confirm the alteration")));
        itemLore.add(Text.of(Chat.embedColours("&7Once clicked this cannot be reversed.")));
        itemLore.add(Text.of(Chat.embedColours("")));
        itemLore.add(Text.of(Chat.embedColours("&7Cost: &bP" + MainConfig.getUnbreedableCost())));

        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .quantity(1)
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&a&lConfirm Alteration")))
                .add(Keys.ITEM_LORE, itemLore)
                .build());
    }

    private static Icon closeIcon(int slot) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .quantity(1)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&c&lGo Back")))
                .build());
    }

    private static Icon dividerIcon(int slot) {
        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&7A Pok\u00E9mon that is marked unbreedable will not be able to breed with other Pok\u00E9mon to make eggs.")));
        itemLore.add(Text.of(Chat.embedColours("&7This is a permanent change and cannot be undone at the moment.")));

        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:spell_tag").orElse(ItemTypes.BARRIER))
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&cUnbreedable Status")))
                .add(Keys.ITEM_LORE, itemLore)
                .build());
    }

    private Icon pokemonIcon(int slot, Pokemon pokemon, int s) {
        ArrayList<Text> itemLore = new ArrayList<>();
        if (!this.confirm) {
            if (!UNBREEDABLE.matches(pokemon)) {
                itemLore.add(Text.of(Chat.embedColours("&bClick here to make this Pok\u00E9mon unbreedable!")));
                if (MainConfig.getUnbreedableCost() != 0)
                    itemLore.add(Text.of(Chat.embedColours("&bCost: &fP" + MainConfig.getUnbreedableCost())));
            } else {
                itemLore.add(Text.of(Chat.embedColours("&cThis Pok\u00E9mon is already unbreedable!")));
            }
        }

        ItemStack item = ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:pixelmon_sprite").orElse(ItemTypes.STAINED_HARDENED_CLAY))
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&a&lSlot " + s + ": " + pokemon.getSpecies().name)))
                .add(Keys.ITEM_LORE, itemLore)
                .build();

        return new Icon(slot, ItemStack.builder().fromContainer(item.toContainer().set(DataQuery.of("UnsafeData","SpriteName"), this.getSprite(pokemon))).build());
    }

    private String getSprite(Pokemon pokemon) {
        String spriteData;

        if(pokemon.isEgg()) {
            spriteData = "pixelmon:" + GuiResources.getEggSprite(pokemon.getSpecies(), pokemon.getEggCycles());
        }else {
            spriteData = "pixelmon:" + GuiResources.getSpritePath(pokemon.getSpecies(), pokemon.getForm(), pokemon.getGender(), pokemon.getCustomTexture(), pokemon.isShiny());
        }

        return spriteData;
    }
}

