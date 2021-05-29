package com.m1zark.pixelmoncommands.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.m1zark.pixelmoncommands.PCInfo;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.commands.*;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

public class CooldownConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;

    public CooldownConfig() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(PixelmonCommands.getInstance().getConfigDir() + "/cooldowns.conf");
        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(PixelmonCommands.getInstance().getConfigDir())) Files.createDirectory(PixelmonCommands.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode items = main.getNode("Cooldowns");
            items.setComment("!!Do not touch this file!!");

            List<String> hatch = main.getNode("Cooldowns","hatch").getList(TypeToken.of(String.class));
            hatch.forEach(uuid -> {
                String[] parts = uuid.split(":");
                Hatch.hatchCooldowns.put(parts[0], Long.valueOf(parts[1]));
            });

            List<String> fossil = main.getNode("Cooldowns","fossil").getList(TypeToken.of(String.class));
            fossil.forEach(uuid -> {
                String[] parts = uuid.split(":");
                Fossil.fossilCooldowns.put(parts[0], Long.valueOf(parts[1]));
            });

            List<String> evolve = main.getNode("Cooldowns","evolve").getList(TypeToken.of(String.class));
            evolve.forEach(uuid -> {
                String[] parts = uuid.split(":");
                Evolve.evolveCooldowns.put(parts[0], Long.valueOf(parts[1]));
            });

            List<String> wt = main.getNode("Cooldowns","wondertrade").getList(TypeToken.of(String.class));
            wt.forEach(uuid -> {
                String[] parts = uuid.split(":");
                WT.WTCooldowns.put(parts[0], Long.valueOf(parts[1]));
            });

            List<String> tmtrade = main.getNode("Cooldowns","tmtrade").getList(TypeToken.of(String.class));
            tmtrade.forEach(uuid -> {
                String[] parts = uuid.split(":");
                TMTrade.TMTradeCooldowns.put(parts[0], Long.valueOf(parts[1]));
            });

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            PixelmonCommands.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PCInfo.ERROR_PREFIX, "There was an issue loading the cooldown config...")));
            e.printStackTrace();
            return;
        }

        PixelmonCommands.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PCInfo.PREFIX, "Loading cooldowns...")));
    }

    public void saveConfig() {
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

    public void saveCooldowns(String type, List items) {
        main.getNode("Cooldowns", type).setValue(items);
    }
}
