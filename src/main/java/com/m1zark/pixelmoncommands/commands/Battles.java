package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Money;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.m1zark.pixelmoncommands.utils.BattleBets;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleType;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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

public class Battles implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED,"You must be logged onto the server to run this command."));

        Optional<Player> player2 = args.getOne(Text.of("player"));
        Optional<Integer> bet = args.getOne(Text.of("bet"));

        if(player2.isPresent() && bet.isPresent()) {
            if(bet.get() > 0) {
                Player p1 = (Player) src;
                Player p2 = player2.get();

                if(p1 == p2) throw new CommandException(Text.of(TextColors.RED,"You cannot battle with yourself..."));

                if (BattleRegistry.getBattle((EntityPlayer)p1) != null || BattleRegistry.getBattle((EntityPlayer)p1) != null) {
                    throw new CommandException(Text.of(TextColors.RED,"You or your opponent are currently in battle and cannot use this command."));
                }

                if(!Money.canPay(p1, bet.get()) || Money.canPay(p2, bet.get())) {
                    throw new CommandException(Text.of(TextColors.RED,"You or your opponent do not have enough money to cover this bet."));
                }

                EntityPixelmon player1FirstPokemon = Pixelmon.storageManager.getParty((EntityPlayerMP) p1).getAndSendOutFirstAblePokemon((Entity)p1);
                if (player1FirstPokemon == null) {
                    throw new CommandException(Text.of(TextColors.RED,"You don't have an able Pok\u00E9mon to battle with."));
                }
                PlayerParticipant battleParticipant1 = new PlayerParticipant((EntityPlayerMP) p1, player1FirstPokemon);

                EntityPixelmon player2FirstPokemon = Pixelmon.storageManager.getParty((EntityPlayerMP)p2).getAndSendOutFirstAblePokemon((EntityPlayerMP)p2);
                if (player2FirstPokemon == null) {
                    throw new CommandException(Text.of(TextColors.RED,"Your opponent doesn't have an able Pok\u00E9mon to battle with."));
                }
                PlayerParticipant battleParticipant2 = new PlayerParticipant((EntityPlayerMP) p2, player2FirstPokemon);

                battleParticipant1.startedBattle = true;
                battleParticipant2.startedBattle = true;
                BattleRegistry.startBattle(battleParticipant1, battleParticipant2);

                BattleBets battleBets = new BattleBets(p1, p2, new BattleRules(), bet.get());
                PixelmonCommands.getInstance().battleBets.add(battleBets);
            } else {
                throw new CommandException(Text.of(TextColors.RED, "The bet amount must be bigger than 0."));
            }
        }

        return CommandResult.success();
    }
}
