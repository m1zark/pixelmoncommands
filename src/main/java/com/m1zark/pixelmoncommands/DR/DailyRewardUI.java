package com.m1zark.pixelmoncommands.DR;

import com.google.common.collect.Lists;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.GUI.Icon;
import com.m1zark.m1utilities.api.GUI.InventoryManager;
import com.m1zark.m1utilities.api.Inventories;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.RandomHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

public class DailyRewardUI extends InventoryManager {
    private Player player;
    private int day;
    private boolean claim;
    private String expires;

    public DailyRewardUI(Player p, int day, boolean claim, String expires) {
        super(p, 5, Text.of(Chat.embedColours("&6&lDaily Rewards")));
        this.player = p;
        this.day = day;
        this.claim = claim;
        this.expires = expires;

        this.designSetup();
    }

    private void designSetup() {
        int x = 0, index = 0;

        for(int y = 0; y < 6 && index < 45; ++index) {
            if (x == 9) {
                x = 0;
                ++y;
            }

            if (PixelmonUtils.between(x, 1, 7)) {
                this.addIcon(border(x + 9 * y, DyeColors.BLACK));
                ++x;
            } else {
                this.addIcon(border(x + 9 * y, DyeColors.RED));
                ++x;
            }
        }

        index = 1; x = 1;
        for(int y = 0; y < 6 && index < 29; ++index) {
            if (x == 8) {
                x = 1;
                ++y;
            }

            this.addIcon(reward(x + 9 * y, index));
            x++;
        }

        this.addIcon(confirm(40,this.player));
    }

    private static Icon border(int slot, DyeColor color) {
        return new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("")))
                .add(Keys.DYE_COLOR, color)
                .build());
    }

    private Icon confirm(int slot, Player player) {
        ArrayList<Text> itemLore = new ArrayList<>();
        if(claim) itemLore.add(Text.of(Chat.embedColours("&7Click here to claim your reward.")));
        else itemLore.add(Text.of(Chat.embedColours("&7You can claim rewards again in &a" + expires.replace("0d",""))));

        Icon icon = new Icon(slot, ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .quantity(1)
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&a&lClaim Reward")))
                .add(Keys.ITEM_LORE, itemLore)
                .build());

        icon.addListener(clickable -> {
            if(this.claim) {
                Sponge.getScheduler().createTaskBuilder().execute(() -> {
                    Reward reward = DailyRewardConfig.getReward(this.day);
                    boolean received = true;

                    if (reward != null) {
                        switch (reward.getType()) {
                            case "item":
                                if (!Inventories.giveItem(player, reward.parseItem(), reward.getCount())) {
                                    Chat.sendMessage(player, "&cCould not receive your daily reward due to a full inventory.".replace("{reward}", reward.display()));
                                    received = false;
                                }
                                break;
                            case "command":
                                Sponge.getCommandManager().process(Sponge.getServer().getConsole(), reward.parseCommand(player));
                                break;
                        }

                        if (received) {
                            Chat.sendMessage(player, "&bYou received your " + ordinal(this.day) + " daily reward: &7{reward}".replace("{reward}", reward.display()));
                            DailyRewardCooldowns.saveCooldown(this.player.getUniqueId(), Instant.now().plusSeconds(86400).toEpochMilli() + "," + this.day);
                        }

                        player.closeInventory();
                    }
                }).delayTicks(1L).submit(PixelmonCommands.getInstance());
            } else {
                Chat.sendMessage(this.player, "&cYou have already claimed your reward for the day. Try again later.");
            }
        });

        return icon;
    }

    private Icon reward(int slot, int quantity) {
        ArrayList<Text> itemLore = new ArrayList<>();
        Reward reward = DailyRewardConfig.getReward(quantity);
        if(reward != null) itemLore.add(Text.of(Chat.embedColours("&7" + reward.display())));

        Icon icon = new Icon(slot, ItemStack.builder()
                .itemType(quantity < this.day ? ItemTypes.BARRIER : ItemTypes.DIAMOND)
                .quantity(quantity)
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.DISPLAY_NAME, Text.of(Chat.embedColours("&aDay " + quantity + (quantity < this.day ? " &6Claimed" : ""))))
                .add(Keys.ITEM_LORE, itemLore)
                .build());

        if(this.day == quantity && claim) {
            icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(Enchantment.of(EnchantmentTypes.UNBREAKING, 1)));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        return icon;
    }

    private static String ordinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }
}
