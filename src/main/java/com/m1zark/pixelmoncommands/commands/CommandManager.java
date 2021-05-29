package com.m1zark.pixelmoncommands.commands;

import com.m1zark.pixelmoncommands.PCInfo;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.commands.elements.ColorElement;
import com.m1zark.pixelmoncommands.commands.elements.PlayerElement;
import com.m1zark.pixelmoncommands.commands.elements.ShopsElement;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    public void registerCommands(PixelmonCommands plugin) {
        Sponge.getCommandManager().register(plugin, main, "pixelcmds", "pixelmoncommands", "pcmds");
        Sponge.getCommandManager().register(plugin, tm, "randomtm");
        //Sponge.getCommandManager().register(plugin, rl, "randomlegend", "rl");
        Sponge.getCommandManager().register(plugin, fossil, "redeemfossil");
        Sponge.getCommandManager().register(plugin, mega, "randommega");
        Sponge.getCommandManager().register(plugin, zcrystals, "randomcrystal");
        Sponge.getCommandManager().register(plugin, color, "pokecolor", "pokecolour");
        Sponge.getCommandManager().register(plugin, hatch, "hatch", "phatch", "pokehatch");
        Sponge.getCommandManager().register(plugin, checkegg, "checkegg", "egg");
        //Sponge.getCommandManager().register(plugin, evolve, "evolve", "pokeevolve", "pevolve");
        Sponge.getCommandManager().register(plugin, heal, "pokeheal", "pheal");
        //Sponge.getCommandManager().register(plugin, shops, "shop");
        if(MainConfig.enableWT) Sponge.getCommandManager().register(plugin, wt, "wt", "wondertrade");
        if(MainConfig.enableWT) Sponge.getCommandManager().register(plugin, wtadmin, "wta", "wtadmin");
        //if(MainConfig.enableTMTrade) Sponge.getCommandManager().register(plugin, tmtrade, "tmtrade");
        Sponge.getCommandManager().register(plugin, legend, "lastlegendary", "ll");
        //Sponge.getCommandManager().register(plugin, ceffects, "cleareffects", "ce");
        Sponge.getCommandManager().register(plugin, unbreedable, "unbreedable");
        Sponge.getCommandManager().register(plugin, storage, "storage");
        Sponge.getCommandManager().register(plugin, checkparty, "checkparty");
        //Sponge.getCommandManager().register(plugin, battles, "battlebet", "bb");
        Sponge.getCommandManager().register(plugin, dailyreward, "dr", "dailyreward");
        if (Sponge.getPluginManager().isLoaded("enjin-minecraft-plugin")) {
            Sponge.getCommandManager().register(plugin, votes, "topvotes");
        }

        PixelmonCommands.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(PCInfo.PREFIX, "Registering commands...")));
    }

    Map arg1 = new HashMap<String, Integer>(){{put("1",1);put("2",2);put("3",3);put("4",4);put("5",5);put("6",6);}};
    Map arg2 = new HashMap<String, String>(){{put("view","view");put("add","add");put("generate","generate");}};

    private CommandSpec dailyreward = CommandSpec.builder()
            .executor(new DailyReward())
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.player(Text.of("player"))),
                    GenericArguments.optionalWeak(
                            GenericArguments.requiringPermission(GenericArguments.choices(Text.of("reload"),new HashMap<String, String>(){{put("reload","reload");}}),"pixelcommands.admin.dailyreward")
                    )
            )
            .permission("pixelcommands.player.dailyreward")
            .build();

    private CommandSpec battles = CommandSpec.builder()
            .arguments(
                    GenericArguments.playerOrSource(Text.of("player")),
                    GenericArguments.integer(Text.of("bet"))
            )
            .executor(new Battles())
            .permission("pixelcommands.player.betting")
            .build();

    private CommandSpec checkparty = CommandSpec.builder()
            .executor(new CheckParty())
            .permission("pixelcommands.player.checkparty")
            .build();

    private CommandSpec votes = CommandSpec.builder()
            .executor(new TopVotes())
            .permission("pixelcommands.admin.topvotes")
            .build();

    private CommandSpec ceffects = CommandSpec.builder()
            .arguments(
                    GenericArguments.optionalWeak(GenericArguments.integer(Text.of("radius")))
            )
            .executor(new ClearEffects())
            .permission("pixelcommands.admin.cleareffects")
            .build();

    private CommandSpec storage_claim = CommandSpec.builder()
            .permission("pixelcommands.player.storage")
            .executor(new Storage.claimPokemon())
            .build();

    private CommandSpec storage_admin = CommandSpec.builder()
            .permission("pixelcommands.admin.storage")
            .executor(new Storage())
            .arguments(
                    new PlayerElement(Text.of("player")),
                    GenericArguments.remainingJoinedStrings(Text.of("slots"))
            )
            .build();

    private CommandSpec storage_list = CommandSpec.builder()
            .permission("pixelcommands.admin.storage")
            .executor(new Storage.listStorage())
            .build();

    private CommandSpec storage = CommandSpec.builder()
            .child(storage_claim, "claim")
            .child(storage_admin, "send")
            .child(storage_list, "list")
            .build();

    private CommandSpec unbreedable = CommandSpec.builder()
            .permission("pixelcommands.player.unbreedable")
            .executor(new Unbreedable())
            .build();

    private CommandSpec legend = CommandSpec.builder()
            .executor(new Legendary())
            .permission("pixelcommands.player.legend")
            .build();

    private CommandSpec shops = CommandSpec.builder()
            .arguments(new ShopsElement(Text.of("shopID")))
            .executor(new Shops())
            .permission("pixelcommands.player.shop")
            .description(Text.of("Opens up the specified shop keeper."))
            .build();

    private CommandSpec heal = CommandSpec.builder()
            .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
            .executor(new Heal())
            .permission("pixelcommands.player.heal")
            .description(Text.of(""))
            .build();

    private CommandSpec evolve = CommandSpec.builder()
            .arguments(
                    GenericArguments.optional(GenericArguments.playerOrSource(Text.of("player"))),
                    GenericArguments.choices(Text.of("slot"), arg1)
            )
            .executor(new Evolve())
            .permission("pixelcommands.player.evolve")
            .description(Text.of("Allows you to force evolve a specific party member."))
            .build();

    private CommandSpec hatch = CommandSpec.builder()
            .arguments(GenericArguments.choices(Text.of("slot"), arg1))
            .executor(new Hatch())
            .permission("pixelcommands.player.hatch")
            .description(Text.of("Allows you to hatch a Pokemon egg."))
            .build();

    private CommandSpec checkegg = CommandSpec.builder()
            .arguments(GenericArguments.choices(Text.of("slot"), arg1))
            .executor(new CheckEgg())
            .permission("pixelcommands.player.checkegg")
            .description(Text.of("Allows you to check a Pokemon egg."))
            .build();

    private CommandSpec color = CommandSpec.builder()
            .arguments(
                    GenericArguments.choices(Text.of("slot"), arg1),
                    new ColorElement(Text.of("color"))
            )
            .executor(new PokeColor())
            .permission("pixelcommands.player.pokecolor")
            .description(Text.of("Allows you to color your pokemon's name."))
            .build();

    private CommandSpec fossil = CommandSpec.builder()
            .executor(new Fossil())
            .permission("pixelcommands.player.redeemfossil")
            .description(Text.of("Takes players fossil and turns it into a WTPokemon."))
            .build();

    private CommandSpec wtpool = CommandSpec.builder()
            .executor(new WT.WTPool())
            .permission("pixelcommands.player.wt")
            .build();

    private CommandSpec wt = CommandSpec.builder()
            .executor(new WT())
            .permission("pixelcommands.player.wt")
            .description(Text.of(""))
            .child(wtpool, "pool")
            .build();

    private CommandSpec wtadmin = CommandSpec.builder()
            .executor(new WT.WTAdmin())
            .arguments(
                    GenericArguments.choices(Text.of("type"), arg2),
                    GenericArguments.optionalWeak(GenericArguments.choices(Text.of("slot"), arg1))
            )
            .permission("pixelcommands.admin.wt")
            .build();

    private CommandSpec rl = CommandSpec.builder()
            .arguments(GenericArguments.flags().flag("s").buildWith(GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of("player")))))
            .executor(new RL())
            .permission("pixelcommands.admin.randomlegend")
            .description(Text.of("Gives player a random legendary pokemon."))
            .build();

    private CommandSpec tm = CommandSpec.builder()
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .executor(new TM())
            .permission("pixelcommands.admin.randomtm")
            .description(Text.of("Gives player a random TM."))
            .build();

    private CommandSpec tmtrade = CommandSpec.builder()
            .executor(new TMTrade())
            .permission("pixelcommands.player.tmtrade")
            .description(Text.of("Allows players to trade a tm for a randomly selected one."))
            .build();

    private CommandSpec mega = CommandSpec.builder()
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .executor(new Mega())
            .permission("pixelcommands.admin.randommega")
            .description(Text.of("Gives players a random mega stone."))
            .build();

    private CommandSpec zcrystals = CommandSpec.builder()
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                    GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
            )
            .executor(new ZCrystal())
            .permission("pixelcommands.admin.randomzcrystal")
            .description(Text.of("Gives players a random z crystal."))
            .build();

    private CommandSpec disguise = CommandSpec.builder()
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of("player"))),
                    GenericArguments.flags().flag("s").buildWith(GenericArguments.withSuggestions(GenericArguments.string(Text.of("pokemon")), EnumSpecies.getNameList()))
            )
            .executor(new Give.disguise())
            .permission("pixelcommands.admin.givr")
            .description(Text.of("Gives player a redeemable item to unlock pokemon disguises."))
            .build();

    private CommandSpec xp = CommandSpec.builder()
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.playerOrSource(Text.of("player"))),
                    GenericArguments.choices(Text.of("type"), new HashMap<String,String>(){{ this.put("player", "player");this.put("all", "all"); }}),
                    GenericArguments.integer(Text.of("duration"))
            )
            .executor(new Give.xp())
            .permission("pixelcommands.admin.give")
            .description(Text.of("Gives player a redeemable item to unlock double xp."))
            .build();

    private CommandSpec give = CommandSpec.builder()
            .child(disguise, "disguise")
            .child(xp, "xp")
            .build();

    private CommandSpec reload = CommandSpec.builder()
            .permission("pixelcommands.admin.reload")
            .executor(new Reload())
            .build();

    private CommandSpec main = CommandSpec.builder()
            .executor(new com.m1zark.pixelmoncommands.commands.PixelmonCommands())
            .description(Text.of("Base command for PixelmonCommands."))
            .child(reload, "reload")
            .child(give, "give")
            .build();
}
