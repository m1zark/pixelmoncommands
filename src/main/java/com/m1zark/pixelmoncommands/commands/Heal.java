package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class Heal implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        EntityPlayerMP player = (EntityPlayerMP) src;
        Optional<Player> playerOptional = args.getOne(Text.of("player"));
        PlayerPartyStorage storage;

        if(playerOptional.isPresent()) {
            if(!src.hasPermission("pixelcommands.others.heal")) {
                throw new CommandException(Text.of(TextColors.RED,"You do not have permission to heal other players Pok\u00E9mon."));
            }

            if (BattleRegistry.getBattle((EntityPlayerMP) playerOptional.get()) != null) {
                throw new CommandException(Text.of(TextColors.RED,"Cannot heal %s's Pok\u00E9mon while they are in battle!".replace("%s", playerOptional.get().getName())));
            }

            storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) playerOptional.get());
            if (storage != null) {
                storage.getTeam().forEach((pokemon) -> { pokemon.heal(); });
            }

            Chat.sendMessage(src, "&7Your Pok\u00E9mon have been fully healed!");
        } else {
            if (BattleRegistry.getBattle(player) != null) {
                throw new CommandException(Text.of(TextColors.RED,"Cannot heal your Pok\u00E9mon while they are in battle!"));
            }

            storage = PixelmonUtils.getPlayerStorage(player);
            if (storage != null) storage.getTeam().forEach((pokemon) -> { pokemon.heal(); });
            Chat.sendMessage(src, "&7Successfully healed %s's Pok\u00E9mon!".replace("%s", player.getName()));
        }

        return CommandResult.success();
    }
}
