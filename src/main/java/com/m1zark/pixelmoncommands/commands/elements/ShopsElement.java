package com.m1zark.pixelmoncommands.commands.elements;

import com.m1zark.pixelmoncommands.Config.MainConfig;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ServerNPCRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.registry.ShopkeeperData;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ShopsElement extends CommandElement {
    public ShopsElement(Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource commandSource, CommandArgs commandArgs) throws ArgumentParseException {
        return commandArgs.next();
    }

    @Override
    public List<String> complete(CommandSource commandSource, CommandArgs commandArgs, CommandContext commandContext) {
        List<String> shops = new ArrayList<>();
        for (ShopkeeperData sk : ServerNPCRegistry.getEnglishShopkeepers()) {
            shops.add(sk.id);
        }

        MainConfig.getBlockedShops().forEach(shop -> shops.removeIf(name -> name.equalsIgnoreCase(shop)));

        return shops;
    }
}
