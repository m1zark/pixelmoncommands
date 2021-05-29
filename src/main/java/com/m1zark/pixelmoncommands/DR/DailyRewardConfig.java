package com.m1zark.pixelmoncommands.DR;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DailyRewardConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public DailyRewardConfig() {
        this.loadConfig();
    }

    private void loadConfig() {
        Path dir = Paths.get(PixelmonCommands.getInstance().getConfigDir() + "/dailyreward");
        Path configFile = Paths.get(dir + "/settings.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(dir)) Files.createDirectory(dir);
            if (!Files.exists(configFile)) Files.createFile(configFile);
            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode daily = main.getNode("Rewards");

            for(int i = 1; i <= 28; i++) {
                daily.getNode(String.valueOf(i),"type").getString("item");
                daily.getNode(String.valueOf(i),"id").getString("minecraft:stone");
                daily.getNode(String.valueOf(i),"count").getInt(5);
            }

            CommentedConfigurationNode settings = main.getNode("Settings");
            //settings.getNode("enable-reset").setComment("Resets the rewards back to day 1 if they miss a day.").getBoolean(true);

            loader.save(main);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveConfig() {
        try {
            loader.save(main);
        } catch (IOException var1) {
            var1.printStackTrace();
        }
    }

    public void reload() {
        try {
            main = loader.load();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    static Reward getReward(int day) {
        CommentedConfigurationNode reward = main.getNode("Rewards",String.valueOf(day));

        if(!reward.isVirtual()) {
            String type = reward.getNode("type").getString();
            String item = reward.getNode("id").isVirtual() ? null : reward.getNode("id").getString();
            String command = reward.getNode("command").isVirtual() ? null : reward.getNode("command").getString();
            Integer count = reward.getNode("count").isVirtual() ? 1 : reward.getNode("count").getInt();
            String display = reward.getNode("display").isVirtual() ? null : reward.getNode("display").getString();

            Integer meta = reward.getNode("data", "meta").isVirtual() ? null : reward.getNode("data", "meta").getInt();
            boolean unbreakable = !reward.getNode("data", "unbreakable").isVirtual() && reward.getNode("data", "unbreakable").getBoolean();

            List<String> lore = Lists.newArrayList();
            if (!reward.getNode("data", "lore").isVirtual()) {
                try {
                    lore = reward.getNode("data", "lore").getList(TypeToken.of(String.class));
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }

            Map nbt = new LinkedHashMap();
            if (!reward.getNode("data", "nbt").isVirtual() && reward.getNode("data", "nbt").getValue() instanceof LinkedHashMap) {
                nbt = (LinkedHashMap) reward.getNode("data", "nbt").getValue();
            }

            return new Reward(day, type, item, command, count, display, meta, nbt, unbreakable, lore);
        } else {
            return null;
        }
    }
}
