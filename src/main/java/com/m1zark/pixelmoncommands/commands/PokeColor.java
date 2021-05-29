package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PokeColor implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        Optional<Integer> slot = args.getOne(Text.of("slot"));
        Optional<String> color = args.getOne(Text.of("color"));

        slot.ifPresent(s -> {
            if(s < 1 || s > 6) {
                Chat.sendMessage(src, "&cYou must enter a slot number between 1 and 6.");
                return;
            }

            PlayerPartyStorage pStorage = PixelmonUtils.getPlayerStorage((EntityPlayerMP) src);
            if (pStorage == null) { return; }

            TextFormatting format = TextFormatting.getValueByName(color.get());
            if(format == null) {
                Chat.sendMessage(src, "&cYou entered an invalid color... stop being dumb.");
                return;
            }

            Pokemon pixelmon = pStorage.get(s - 1);
            if(pixelmon != null) {
                String name = pixelmon.getDisplayName();
                if (name.startsWith("\u00a7")) name = name.substring(2);
                pixelmon.setNickname(format + name + TextFormatting.WHITE);
            }
        });

        return CommandResult.success();
    }
}
