package com.m1zark.pixelmoncommands.utils;

import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.entity.living.player.Player;

@Getter @Setter
@RequiredArgsConstructor
public class BattleBets {
    public final Player player1;
    public final Player player2;
    public final BattleRules rules;
    public final int betAmount;
}
