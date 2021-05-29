package com.m1zark.pixelmoncommands.tasks;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.WT.WTPokemon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class storageNotice {
    private static Task task;

    public static void initialize() {
        task = Task.builder().execute(t -> {
            Sponge.getServer().getOnlinePlayers().forEach(player -> {
                List<WTPokemon> pokemon = PixelmonCommands.getInstance().getSql().getStoragePokemon(player.getUniqueId());

                if(!pokemon.isEmpty()) {
                    Chat.sendMessage(player, "&cYou have Pok\u00E9mon waiting for you... use &7/storage claim &cto retrieve them!");
                }
            });
        }).interval(10, TimeUnit.MINUTES).async().name("StorageNotifier").submit(PixelmonCommands.getInstance());
    }
}
