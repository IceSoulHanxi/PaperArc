package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Collections;

import org.bukkit.craftbukkit.v1_20_R1.block.impl.CraftCandle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Adds getMinimumCandles missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Add-missing-block-data-API.patch (getMin(CANDLES)).
 */
@Mixin(CraftCandle.class)
public abstract class CraftCandleApiMixin {

    @Shadow
    private static IntegerProperty CANDLES;

    @Unique
    public int getMinimumCandles() {
        return Collections.min(CANDLES.getPossibleValues());
    }
}
