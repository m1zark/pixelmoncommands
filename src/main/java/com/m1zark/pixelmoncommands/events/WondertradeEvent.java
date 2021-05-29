package com.m1zark.pixelmoncommands.events;

import com.m1zark.pixelmoncommands.WT.WTPokemon;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.event.cause.Cause;

@Getter
@RequiredArgsConstructor
public class WondertradeEvent extends BaseEvent {
    public final EntityPlayerMP player;
    private final WTPokemon givingPokemon;
    private final WTPokemon receivingPokemon;
    @NonNull
    private final Cause cause;

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
