package com.m1zark.pixelmoncommands.Config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.m1utilities.api.Discord.DiscordOption;
import com.m1zark.m1utilities.api.Discord.Field;
import com.m1zark.pixelmoncommands.PCInfo;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class MainConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    public static CommentedConfigurationNode main;

    public static boolean enableWT;
    public static boolean enableTMTrade;

    public static int maxWTPool;
    public static boolean allowEggs;
    public static boolean enableMessages;

    public MainConfig() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(PixelmonCommands.getInstance().getConfigDir() + "/settings.conf");
        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(PixelmonCommands.getInstance().getConfigDir())) Files.createDirectory(PixelmonCommands.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode general = main.getNode("General");
            CommentedConfigurationNode discord = main.getNode("Discord");
            CommentedConfigurationNode messages = main.getNode("Messages");

            general.setComment("Cooldown times are in seconds. Set to 0 to disable cooldown.");
            general.getNode("Fossil","setCooldown").getInt(600);
            general.getNode("Hatch","setCooldown").getInt(600);
            general.getNode("Evolve","setCooldown").getInt(600);
            general.getNode("WT","setCooldown").getInt(600);
            general.getNode("TMTrade","setCooldown").getInt(600);

            general.getNode("Evolve","blacklist").setComment("List of Pok\u00E9mon to block from being evolved using the command.");
            general.getNode("Evolve","blacklist").getList(TypeToken.of(String.class), Lists.newArrayList("Phione","Bidoof"));

            enableTMTrade = general.getNode("TMTrade","enableTMTradeCommand").getBoolean(false);

            maxWTPool = general.getNode("WT","PoolSize").getInt(50);
            allowEggs = general.getNode("WT","allowEggs").getBoolean(true);
            enableMessages = general.getNode("WT","enableShinyLegendaryMessages").getBoolean(true);
            enableWT = general.getNode("WT","enableWTCommand").getBoolean(true);

            general.getNode("Announcements","enableLegendary").getBoolean(true);
            general.getNode("Announcements","enableShiny").getBoolean(true);
            general.getNode("Announcements","enableBoss").getBoolean(true);
            general.getNode("Announcements","enableDiscordNotifications").getNode(true);
            general.getNode("Misc","disableAFKSpawns").getBoolean(true);
            general.getNode("Misc","enableSpawnFix").getBoolean(true);

            main.getNode("LegendarySpawn","spawn").setComment("!!Don't touch this!!").getString("");

            main.getNode("Unbreedable","cost").getInt(0);

            general.getNode("Shops","blacklist").setComment("List of shops to hide in the /shop command.");
            general.getNode("Shops","blacklist").getList(TypeToken.of(String.class), Lists.newArrayList("pokemartmain1","pokemartmain2","pokemartmain3","pokemartsecond1","pokemartsecond2","spawn1"));

            messages.getNode("Cooldown").getString("&cYou may use this command again in &d{time} &cseconds.");
            messages.getNode("Fossil","NoItem").getString("&cYou need to be holding a fossil in your mainhand to use this.");
            messages.getNode("Fossil","Success").getString("&aYou have successfully redeemed a &3{pokemon}&a.");
            messages.getNode("Fossil","NotAFossil").getString("&cThe item in your hand is not a fossil.");

            messages.getNode("Hatch","InvalidNumber").getString("&cYou must enter a slot number between 1 and 6.");
            messages.getNode("Hatch","EmptySlot").getString("&cYou have nothing in that slot!");
            messages.getNode("Hatch","NoEgg").getString("&cThis slot does not contain an egg.");
            messages.getNode("Hatch","Success").getString("&aYour Egg hatched into a {pokemon}!");

            messages.getNode("Evolve","InvalidNumber").getString("&cYou must enter a slot number between 1 and 6.");
            messages.getNode("Evolve","NoEvolutions").getString("&cThere are no more evolutions for this Pok\u00E9mon!");
            messages.getNode("Evolve","EmptySlot").getString("&cYou have nothing in that slot!");
            messages.getNode("Evolve","OtherPlayers").getString("&cYou don't have permission to evolve other players Pok\u00E9mon.");
            messages.getNode("Evolve","blacklisted").getString("&cSorry, but you cannot use this command on {pokemon}.");
            messages.getNode("Evolve","CantEvolve").getString("&cThat Pok\u00E9mon cannot evolve!");

            messages.getNode("WT","Success").getString("&7{player} has added a &e{type} &7to the WT!");
            messages.getNode("WT","LegendaryShinyAdded").getString("&7{player} has added a &e{type} &7to the WT!");
            messages.getNode("WT","LegendaryShinyReceived").getString("&7{player} received a &e{type} {pokemon} &7from the WT!");
            messages.getNode("WT","GenerateNewPool").getString("&7A new WT Pool has been generated!");

            messages.getNode("LegendarySpawn","spawnMessage").getString("&7A &dLegendary {name} &7last spawned &d{time} &7ago.");

            messages.getNode("RandomMega","Success").getString("&2You received a random &9{stone_name}&2!");
            messages.getNode("RandomLegend","Success").getString("&aYou received a random &d{pokemon}&a!");
            messages.getNode("RandomTM","Success").getString("&2You received &9{count} &2random &9{tm}&2!");
            messages.getNode("RandomZCrystal","Success").getString("&2You received a random &9{crystal_name}&2!");
            messages.getNode("TMTrade","Success").getString("&2You traded your TM for a &9{tm}&2!");

            messages.getNode("Announcements","displayMessage").setComment("{action} = captured/defeated").getString("&f[&cProf. Oak&f]&d {player}&7 has {action} a {shiny}{legendary}{boss} &d{pokemon} &7!");

            discord.getNode("webhook-url").getList(TypeToken.of(String.class));
            discord.getNode("notifications","legendary-spawn","color").getString("#AA00AA");
            discord.getNode("notifications","mega-spawn","color").getString("#AA00AA");
            discord.getNode("notifications","ultra-spawn","color").getString("#AA00AA");

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            PixelmonCommands.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PCInfo.ERROR_PREFIX, "There was an issue loading the config...")));
            e.printStackTrace();
            return;
        }

        PixelmonCommands.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PCInfo.PREFIX, "Loading configuration...")));
    }

    public static void saveConfig() {
        try {
            loader.save(main);
        } catch (IOException var1) {
            var1.printStackTrace();
        }
    }

    public static void saveSpawn(String value) {
        main.getNode("LegendarySpawn","spawn").setValue(value);
    }

    public void reload() {
        try {
            main = loader.load();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static int getCooldownTimes(String command) {
        return main.getNode("General",command,"setCooldown").getInt();
    }

    public static String getLegendSpawn() {
        return main.getNode("LegendarySpawn","spawn").getString();
    }

    public static int getUnbreedableCost() { return main.getNode("Unbreedable","cost").getInt(); }

    public static boolean enableMessages(String value) {
        return main.getNode((Object[])value.split("\\.")).getBoolean();
    }

    public static List<String> getBlocked() {
        try {
            List<String> validBlocks = new ArrayList<>();

            main.getNode("General","Evolve","blacklist").getList(TypeToken.of(String.class)).forEach(name-> {
                if(EnumSpecies.hasPokemon(name)) validBlocks.add(name);
            });

            return validBlocks;
        } catch (ObjectMappingException e) {
            PixelmonCommands.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PCInfo.ERROR_PREFIX, "There was an issue loading the Evolve command blacklist.")));
            return Lists.newArrayList();
        }
    }

    public static List<String> getBlockedShops() {
        try {
            List<String> blacklist = new ArrayList<>();
            Iterator<String> list = main.getNode("General","Shops","blacklist").getList(TypeToken.of(String.class)).iterator();

            list.forEachRemaining(blacklist::add);

            return blacklist;
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }

    public static String getMessages(String value) { return main.getNode((Object[])value.split("\\.")).getString(); }

    public static DiscordOption discordOption(String id, String pokemon, String biome) {
        String username = main.getNode("Discord","notifications",id,"username").isVirtual() ? null : main.getNode("Discord","notifications",id,"username").getString();
        String avatar = main.getNode("Discord","notifications",id,"avatar").isVirtual() ? null : main.getNode("Discord","notifications",id,"avatar").getString();
        String content = main.getNode("Discord","notifications",id,"content").isVirtual() ? null : main.getNode("Discord","notifications",id,"content").getString().replace("{pokemon}",pokemon).replace("{biome}",biome);
        String title = main.getNode("Discord","notifications",id,"title").isVirtual() ? null : main.getNode("Discord","notifications",id,"title").getString().replace("{pokemon}",pokemon).replace("{biome}",biome);
        String description = main.getNode("Discord","notifications",id,"description").isVirtual() ? null : main.getNode("Discord","notifications",id,"description").getString().replace("{pokemon}",pokemon).replace("{biome}",biome);
        String thumbnail = main.getNode("Discord","notifications",id,"thumbnail").isVirtual() ? null : main.getNode("Discord","notifications",id,"thumbnail").getString().replace("{pokemon}",pokemon.toLowerCase());
        String image = main.getNode("Discord","notifications",id,"image").isVirtual() ? null : main.getNode("Discord","notifications",id,"image").getString().replace("{pokemon}",pokemon.toLowerCase());
        boolean timestamp = !main.getNode("Discord","notifications",id,"timestamp").isVirtual() && main.getNode("Discord","notifications",id,"timestamp").getBoolean();

        List<Field> fields = Lists.newArrayList();
        if(!main.getNode("Discord","notifications",id,"fields").isVirtual()) {
            for (int i = 0; i < main.getNode("Discord","notifications",id,"fields").getChildrenList().size(); i++) {
                CommentedConfigurationNode field = main.getNode("Discord","notifications",id,"fields").getChildrenList().get(i);

                fields.add(new Field(
                        field.getNode("name").isVirtual() ? null : field.getNode("name").getString().replace("{pokemon}",pokemon).replace("{biome}",biome),
                        field.getNode("value").isVirtual() ? null : field.getNode("value").getString().replace("{pokemon}",pokemon).replace("{biome}",biome),
                        !field.getNode("inline").isVirtual() && field.getNode("inline").getBoolean()
                ));
            }
        }

        Map<String,String> footer = new HashMap<>();
        if(!main.getNode("Discord","notifications",id,"footer").isVirtual()) {
            footer.put("text", main.getNode("Discord","notifications",id,"footer","text").getString());
            footer.put("icon", main.getNode("Discord","notifications",id,"footer","icon").getString());
        }

        try {
            return DiscordOption.builder()
                    .webhookChannels(main.getNode("Discord","webhook-url").getList(TypeToken.of(String.class)))
                    .username(username)
                    .avatar_url(avatar)
                    .content(content)
                    .color(Color.decode(main.getNode("Discord","notifications",id,"color").getString()))
                    .title(title)
                    .description(description)
                    .fields(fields)
                    .thumbnail(thumbnail)
                    .image(image)
                    .footer(footer)
                    .timestamp(timestamp)
                    .build();
        }catch (ObjectMappingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
