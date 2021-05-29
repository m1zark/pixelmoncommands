package com.m1zark.pixelmoncommands.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClearEffects implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        Optional<Integer> radius = args.getOne(Text.of("radius"));
        int r = radius.orElse(10);

        getClosestPlayersFromLocation((Player) src, r).forEach(entity -> {
            if(!((Player) entity).hasPermission("pixelcommands.keepeffects")) {
                List<PotionEffect> effects = entity.get(Keys.POTION_EFFECTS).get();
                effects.clear();
                entity.offer(Keys.POTION_EFFECTS, effects);
            }
        });

        return CommandResult.success();
    }

    private List<Entity> getClosestPlayersFromLocation(final Player player, final int radius) {
        ArrayList<Entity> arrayList = new ArrayList<>();

        player.getLocation().getExtent().getEntities().forEach(entity -> {
            if(entity.getLocation().getPosition().distance(player.getLocation().getPosition()) <= radius) arrayList.add(entity);
        });

        return arrayList;
    }
}