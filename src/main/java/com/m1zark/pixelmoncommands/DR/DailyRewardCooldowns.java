package com.m1zark.pixelmoncommands.DR;

import com.m1zark.pixelmoncommands.PixelmonCommands;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

public class DailyRewardCooldowns {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public DailyRewardCooldowns() {
        this.loadConfig();
    }

    private void loadConfig() {
        Path dir = Paths.get(PixelmonCommands.getInstance().getConfigDir() + "/dailyreward");
        Path configFile = Paths.get(dir + "/cooldowns.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(dir)) Files.createDirectory(dir);
            if (!Files.exists(configFile)) Files.createFile(configFile);
            if (main == null) main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));

            CommentedConfigurationNode cooldowns = main.getNode("Cooldowns");

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

    static void saveCooldown(UUID uuid, String time) {
        main.getNode("Cooldowns",uuid.toString()).setValue(time);
        saveConfig();
    }

    public static HashMap<UUID,String> getCooldown() {
        HashMap<UUID,String> data = new HashMap<>();

        main.getNode("Cooldowns").getChildrenMap().forEach((uuid,time) -> {
            data.put(UUID.fromString((String)uuid), time.getString());
        });

        return data;
    }
}
