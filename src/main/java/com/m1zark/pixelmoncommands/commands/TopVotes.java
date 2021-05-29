package com.m1zark.pixelmoncommands.commands;

import com.enjin.rpc.mappings.mappings.plugin.statistics.TopVoter;
import com.enjin.sponge.managers.StatSignManager;
import com.m1zark.m1utilities.api.Chat;
import com.m1zark.pixelmoncommands.PixelmonCommands;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

public class TopVotes implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        PixelmonCommands.getInstance().getAsync().execute(() -> {
            if (StatSignManager.fetchStats()) {
                List voters = StatSignManager.getStats().getTopVotersMonth();
                ArrayList<Text> players = new ArrayList<>();
                try {
                    for (int i = 0; i < 10; ++i) {
                        players.add(Text.of((((TopVoter)voters.get(i)).getName() + ": " + ((TopVoter)voters.get(i)).getCount() + " Votes")));
                    }
                }
                catch (NullPointerException i) {
                    // empty catch block
                }
                if (!players.isEmpty()) {
                    PaginationList.builder().contents(players).title(Text.of((Object[])new Object[]{Chat.embedColours(("Top 10 Voters for " + this.getMonthName(2)))})).linesPerPage(10).build().sendTo(src);
                } else {
                    Chat.sendMessage(src, "&7No voter data found.");
                }
            }
        });
        return CommandResult.success();
    }

    private String getMonthName(int monthNumber) {
        String[] months = new DateFormatSymbols().getMonths();
        int n = monthNumber - 1;
        return n >= 0 && n <= 11 ? months[n] : "wrong number";
    }
}
