package com.m1zark.pixelmoncommands;

import com.google.inject.Inject;
import com.m1zark.pixelmoncommands.DR.DailyRewardConfig;
import com.m1zark.pixelmoncommands.DR.DailyRewardCooldowns;
import com.m1zark.pixelmoncommands.Pixelmon.Clauses;
import com.m1zark.pixelmoncommands.WT.WTPokemon;
import com.m1zark.pixelmoncommands.commands.*;
import com.m1zark.pixelmoncommands.listeners.PixelmonListeners;
import com.m1zark.pixelmoncommands.listeners.PlayerListeners;
import com.m1zark.pixelmoncommands.tasks.storageNotice;
import com.m1zark.pixelmoncommands.utils.BattleBets;
import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.m1zark.pixelmoncommands.Config.CooldownConfig;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.m1zark.pixelmoncommands.storage.DataSource;
import com.pixelmonmod.pixelmon.Pixelmon;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.*;

@Getter
@Plugin(id=PCInfo.ID, name=PCInfo.NAME, version=PCInfo.VERSION,description=PCInfo.DESCRIPTION,authors="m1zark")
public class PixelmonCommands {
    @Inject
    private Logger logger;
    @Inject private PluginContainer pC;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private MainConfig config;
    private CooldownConfig cdConfig;
    private DailyRewardConfig dailyRewardConfig;
    private DailyRewardCooldowns dailyRewardCooldowns;
    private DataSource sql;
    private static PixelmonCommands instance;
    @Inject private PluginContainer pluginContainer;
    private boolean enabled = true;
    private SpongeExecutorService sync;
    private SpongeExecutorService async;

    public NucleusAFKService afkService;

    public List<WTPokemon> WTPool = new ArrayList<>();
    public List<BattleBets> battleBets = new ArrayList<>();

    @Listener public void onServerStart(GameInitializationEvent e){
        instance = this;
        PCInfo.startup();

        this.enabled = PCInfo.dependencyCheck();

        if(this.enabled) {
            this.sync = Sponge.getScheduler().createSyncExecutor(this);
            this.async = Sponge.getScheduler().createAsyncExecutor(this);
            this.config = new MainConfig();
            new CommandManager().registerCommands(this);
            this.cdConfig = new CooldownConfig();
            this.dailyRewardConfig = new DailyRewardConfig();
            this.dailyRewardCooldowns = new DailyRewardCooldowns();

            Sponge.getEventManager().registerListeners(this, new PlayerListeners());
            Pixelmon.EVENT_BUS.register(new PixelmonListeners());
            new Clauses();

            // Initialize data source and creates tables
            this.sql = new DataSource("WT_POOL","STORAGE");
            this.sql.createTables();
        }
    }

    @Listener public void onGameStarting(GameStartingServerEvent event) {
        if(this.enabled) {
            afkService = Sponge.getServiceManager().provideUnchecked(NucleusAFKService.class);
        }
    }

    @Listener(order = Order.POST) public void postGameStart(GameStartedServerEvent event){
        if(getSql().getPool().isEmpty()) {
            PixelmonUtils.generateRandomPool(MainConfig.maxWTPool);
        }
        getWTPool().addAll(getSql().getPool());

        storageNotice.initialize();
    }

    @Listener public void onReload(GameReloadEvent e) {
        if (this.enabled) {
            this.config = new MainConfig();
            this.dailyRewardConfig = new DailyRewardConfig();
            this.dailyRewardCooldowns = new DailyRewardCooldowns();
            getConsole().ifPresent(console -> console.sendMessages(Text.of(PCInfo.PREFIX, "Configurations have been reloaded")));
        }
    }

    @Listener public void onServerStop(GameStoppingServerEvent e) {
        getSql().clearTables();
        getWTPool().forEach(pkm -> getSql().addPokemon(pkm));
    }

    @Listener public void onServerStop(GameStoppingEvent e) {
        try {
            this.sql.shutdown();
        } catch (Exception error) {
            error.printStackTrace();
        }

        List<String> hatchUUIDs = new ArrayList<>();
        Hatch.hatchCooldowns.forEach((k,v) -> hatchUUIDs.add(k + ":" + v));
        this.cdConfig.saveCooldowns("hatch", hatchUUIDs);

        List<String> fossilUUIDs = new ArrayList<>();
        Fossil.fossilCooldowns.forEach((k,v) -> fossilUUIDs.add(k + ":" + v));
        this.cdConfig.saveCooldowns("fossil", fossilUUIDs);

        List<String> evolveUUIDs = new ArrayList<>();
        Evolve.evolveCooldowns.forEach((k,v) -> evolveUUIDs.add(k + ":" + v));
        this.cdConfig.saveCooldowns("evolve", evolveUUIDs);

        List<String> wtUUIDs = new ArrayList<>();
        WT.WTCooldowns.forEach((k,v) -> wtUUIDs.add(k + ":" + v));
        this.cdConfig.saveCooldowns("wondertrade", wtUUIDs);

        List<String> tmUUIDs = new ArrayList<>();
        TMTrade.TMTradeCooldowns.forEach((k,v) -> tmUUIDs.add(k + ":" + v));
        this.getCdConfig().saveCooldowns("tmtrade", tmUUIDs);

        this.cdConfig.saveConfig();
        getConsole().ifPresent(console -> console.sendMessages(Text.of(PCInfo.PREFIX, TextColors.GREEN, "Saving Cooldowns to file...")));
    }

    public static PixelmonCommands getInstance() { return instance; }

    public Optional<ConsoleSource> getConsole() {
        return Optional.ofNullable(Sponge.isServerAvailable() ? Sponge.getServer().getConsole() : null);
    }
}
