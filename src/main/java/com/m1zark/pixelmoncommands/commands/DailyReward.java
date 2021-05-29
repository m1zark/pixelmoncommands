package com.m1zark.pixelmoncommands.commands;

import com.m1zark.m1utilities.api.Chat;
import com.m1zark.m1utilities.api.Time;
import com.m1zark.pixelmoncommands.DR.DailyRewardCooldowns;
import com.m1zark.pixelmoncommands.DR.DailyRewardUI;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class DailyReward implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, "You must be logged onto the server to run this command."));
        Player player = (Player) args.getOne("player").orElse(src);

        if(args.getOne("reload").isPresent()) {
            PixelmonCommands.getInstance().getDailyRewardConfig().reload();
            PixelmonCommands.getInstance().getDailyRewardCooldowns().reload();

            Chat.sendMessage(src, "&7DailyReward configs successfully reloaded.");

            return CommandResult.success();
        }

        if(!player.equals((Player) src)) throw new CommandException(Text.of(TextColors.RED, "You cannot open the menu of another player."));

        if (BattleRegistry.getBattle((EntityPlayerMP) player) != null) {
            throw new CommandException(Text.of(TextColors.RED,"You cannot open this menu while you are in battle!"));
        }

        int daily = 1;
        boolean claim = true;
        String expires = "";

        if(DailyRewardCooldowns.getCooldown().containsKey(player.getUniqueId())) {
            String[] day = DailyRewardCooldowns.getCooldown().get(player.getUniqueId()).split(",");
            expires = new Time(Long.parseLong(day[0])).toString("%dd %dh %dm %ds");

            int daysBetween2 = daysBetween(dateToCalendar(new Date(Long.parseLong(day[0]))), dateToCalendar(new Date()));

            if(!expires.equals("Expired")) claim = false;
            if(daysBetween2 == 1) daily = Integer.parseInt(day[1]) + 1;
            if(Integer.parseInt(day[1]) == 28) daily = 1;
        }

        player.openInventory((new DailyRewardUI(player, daily, claim, expires).getInventory()));

        return CommandResult.success();
    }

    private static long betweenDates(Date firstDate, Date secondDate) {
        return ChronoUnit.DAYS.between(firstDate.toInstant(), secondDate.toInstant());
    }

    private Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    private static int daysBetween(Calendar day1, Calendar day2){
        Calendar dayOne = (Calendar) day1.clone(), dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays ;
        }
    }
}
