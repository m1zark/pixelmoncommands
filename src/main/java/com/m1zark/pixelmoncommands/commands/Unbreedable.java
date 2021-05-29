package com.m1zark.pixelmoncommands.commands;

import com.m1zark.pixelmoncommands.UI.UnbreedableUI;
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

public class Unbreedable implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        if (BattleRegistry.getBattle((EntityPlayerMP)src) != null) {
            throw new CommandException(Text.of(TextColors.RED, "Cannot use this while in battle!"));
        }

        PlayerPartyStorage storage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) src);
        if(storage != null) {
            storage.retrieveAll();
            ((Player) src).openInventory((new UnbreedableUI((Player) src)).getInventory());
        }

        return CommandResult.success();
    }
}