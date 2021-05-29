package com.m1zark.pixelmoncommands.Pixelmon;

import com.pixelmonmod.pixelmon.battles.rules.clauses.*;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.SereneGrace;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;

public class Clauses {
    public Clauses() {
        BattleClauseRegistry.getClauseRegistry().registerCustomClause(
                new BattleClauseSingleAll("endless", new ItemPreventClause("", EnumHeldItems.leppa), new MoveClause("", "Recycle"), new MoveClause("", "Milk Drink", "Moonlight", "Morning Sun", "Recover", "Roost", "Slack Off", "Soft-Boiled", "Wish"))
        );
    }
}
