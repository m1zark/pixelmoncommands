package com.m1zark.pixelmoncommands.WT;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.m1utilities.api.GUI.InventoryManager;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.pixelmoncommands.commands.WT;
import com.m1zark.pixelmoncommands.events.WondertradeEvent;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WonderTradeUI extends InventoryManager {
    private Player player;
    private PlayerPartyStorage storage;
    private boolean confirm = false;
    private int slot;

    public WonderTradeUI(Player p) {
        super(p, 3, Text.of(Chat.embedColours("&5&lWonderTrade")));
        this.player = p;
        this.storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) p);

        this.setupDesign();
    }

    public WonderTradeUI(Player p, boolean confirm, int slot) {
        super(p, 3, Text.of(Chat.embedColours("&5&lWonderTrade &l&0\u27A5&r &8Confirmation")));
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
            com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon = storage.get(this.slot);
            PokemonData pkmn = new PokemonData(pokemon);

            Icon confirmIcon = confirmIcon(11, this.player);
            confirmIcon.addListener(clickable ->  {
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    this.confirmExchange(this.slot);
                    this.player.closeInventory();
                }).delayTicks(1L).submit(PixelmonCommands.getInstance());
            });
            this.addIcon(confirmIcon);

            this.addIcon(new Icon(13, pkmn.getSprite("", this.player)));

            Icon close = closeIcon(15);
            close.addListener(clickable ->  {
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    this.player.openInventory((new WonderTradeUI(this.player)).getInventory());
                }).delayTicks(1L).submit(PixelmonCommands.getInstance());
            });
            this.addIcon(close);
        } else {
            for (int slot = 10; slot <= 16; slot++) {
                if (slot == 13) {
                    this.addIcon(infoIcon(slot, this.player));
                } else {
                    com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon = storage.get(s);
                    PokemonData pkmn = new PokemonData(pokemon);

                    final int sl = s;

                    if (pokemon != null) {
                        Icon poke = new Icon(slot, pkmn.getSprite("", this.player));
                        poke.addListener(clickable ->  {
                            if (!pkmn.isEgg()) {
                                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                    this.player.openInventory((new WonderTradeUI(this.player, true, sl)).getInventory());
                                }).delayTicks(1L).submit(PixelmonCommands.getInstance());
                            } else {
                                if (MainConfig.allowEggs) {
                                    Sponge.getScheduler().createTaskBuilder().execute(() -> {
                                        this.player.openInventory((new WonderTradeUI(this.player, true, sl)).getInventory());
                                    }).delayTicks(1L).submit(PixelmonCommands.getInstance());
                                } else {
                                    Chat.sendMessage(this.player, "&7You can't add eggs to the wonder trade.");
                                }
                            }
                        });
                        this.addIcon(poke);
                    } else {
                        this.addIcon(BorderIcon(slot, DyeColors.GRAY, "Slot " + (s + 1) + " is empty!"));
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

    private static Icon infoIcon(int slot, Player player) {
        List<WTPokemon> pokemon = PixelmonCommands.getInstance().WTPool;

        List<String> legend = EnumSpecies.legendaries;
        legend.removeIf(name -> name.equals("Phione"));

        long legendaries = pokemon.stream().filter(p -> legend.contains(p.getPokemon().getSpecies().name) || EnumSpecies.ultrabeasts.contains(p.getPokemon().getSpecies().name)).count();
        long shiny = pokemon.stream().filter(p -> p.getPokemon().isShiny()).count();

        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&7WT allows you to trade one of your Pok\u00E9mon for a random Pok\u00E9mon.")));
        itemLore.add(Text.of(Chat.embedColours("&7Clicking on a Pok\u00E9mon will take you to the confirmation page.")));
        itemLore.add(Text.of(Chat.embedColours("")));
        itemLore.add(Text.of(Chat.embedColours("&7Pok\u00E9mon in WT: &b" + pokemon.size())));
        itemLore.add(Text.of(Chat.embedColours("&7Legendary Pok\u00E9mon: &b" + legendaries)));
        itemLore.add(Text.of(Chat.embedColours("&7Shiny Pok\u00E9mon: &b" + shiny)));

        return new Icon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_panel").orElse(ItemTypes.BARRIER))
                .quantity(1)
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&a&lWonderTrade Help")))
                .add(Keys.ITEM_LORE, itemLore)
                .build());
    }

    private static Icon confirmIcon(int slot, Player player) {
        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(Chat.embedColours("&7Clicking this button will confirm the trade")));
        itemLore.add(Text.of(Chat.embedColours("&7Once clicked this cannot be reversed.")));

        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .quantity(1)
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&a&lConfirm Trade")))
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

    private void confirmExchange(int slot) {
        final PokemonSpec UNTRADABLE = new PokemonSpec("untradeable");

        PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) this.player);
        if (storage == null) { return; }

        Pokemon sentPokemon = storage.get(slot);

        if(UNTRADABLE.matches(sentPokemon)) {
            this.player.sendMessage(Text.of(TextColors.GRAY, "This Pok\u00E9mon is marked as untradeable, and cannot be used on wondertrade..."));
            return;
        }

        if (storage.getTeam().size() == 1 && !sentPokemon.isEgg()) {
            Chat.sendMessage(this.player, "&7You can't wondertrade your last able Pok\u00E9mon.");
            return;
        }

        List<WTPokemon> pool = PixelmonCommands.getInstance().WTPool;
        WTPokemon receivingPokemon = pool.get(RandomHelper.getRandomNumberBetween(0, pool.size() - 1));
        WTPokemon givingPokemon = new WTPokemon(receivingPokemon.getId(), PixelmonUtils.getNbt(sentPokemon).toString());

        WondertradeEvent event = new WondertradeEvent((EntityPlayerMP)this.player, givingPokemon, receivingPokemon, Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, PixelmonCommands.getInstance().getPluginContainer()).add(EventContextKeys.PLAYER_SIMULATED, (this.player).getProfile()).build(), PixelmonCommands.getInstance()));
        Sponge.getEventManager().post(event);

        if(!event.isCancelled()) {
            PixelmonCommands.getInstance().WTPool.remove(receivingPokemon);
            PixelmonCommands.getInstance().WTPool.add(givingPokemon);

            storage.set(slot, null);
            storage.add(receivingPokemon.getPokemon());

            Chat.sendMessage(this.player, MainConfig.getMessages("Messages.WT.Success")
                    .replace("{pokemon1}", givingPokemon.getPokemon().getSpecies().name())
                    .replace("{pokemon2}", receivingPokemon.getPokemon().getSpecies().name())
            );

            List<String> legendaries = EnumSpecies.legendaries;
            legendaries.removeIf(name -> name.equals("Phione"));

            if (MainConfig.enableMessages) {
                if (givingPokemon.getPokemon().isShiny() || legendaries.contains(givingPokemon.getPokemon().getSpecies().name) || EnumSpecies.ultrabeasts.contains(givingPokemon.getPokemon().getSpecies().name)) {
                    if (!givingPokemon.getPokemon().isEgg()) {
                        Chat.sendBroadcastMessage(this.player, MainConfig.getMessages("Messages.WT.LegendaryShinyAdded")
                                .replace("{player}", this.player.getName())
                                .replace("{type}", givingPokemon.getPokemon().isShiny() ? "Shiny" : legendaries.contains(givingPokemon.getPokemon().getSpecies().name) ? "Legendary" : "Ultra Beast")
                        );
                    }
                }

                if (receivingPokemon.getPokemon().isShiny() || legendaries.contains(receivingPokemon.getPokemon().getSpecies().name)  || EnumSpecies.ultrabeasts.contains(receivingPokemon.getPokemon().getSpecies().name)) {
                    if (!receivingPokemon.getPokemon().isEgg()) {
                        Chat.sendBroadcastMessage(this.player, MainConfig.getMessages("Messages.WT.LegendaryShinyReceived")
                                .replace("{player}", this.player.getName())
                                .replace("{type}", receivingPokemon.getPokemon().isShiny() ? "Shiny" : legendaries.contains(receivingPokemon.getPokemon().getSpecies().name) ? "Legendary" : "Ultra Beast")
                                .replace("{pokemon}", receivingPokemon.getPokemon().getSpecies().name())
                        );
                    }
                }
            }

            WT.WTCooldowns.put(player.getUniqueId().toString(), Instant.now().plusSeconds(MainConfig.getCooldownTimes("WT")).toEpochMilli());
        }
    }
}
