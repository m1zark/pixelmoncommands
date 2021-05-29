package com.m1zark.pixelmoncommands.WT;

import com.m1zark.pixelmoncommands.utils.PixelmonUtils;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WTPokemon {
    private String nbt;
    private int id;

    public WTPokemon(int id, String nbt) {
        this.id = id;
        this.nbt = nbt;
    }

    public Pokemon getPokemon() {
        return PixelmonUtils.getPokemon(this.nbt);
    }
}
