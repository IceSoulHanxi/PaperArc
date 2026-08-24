package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Collections;

import org.bukkit.craftbukkit.v.block.impl.CraftPinkPetals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Adds getMinimumFlowerAmount missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Add-missing-block-data-API.patch (getMin(FLOWER_AMOUNT)).
 */
@Mixin(CraftPinkPetals.class)
public abstract class CraftPinkPetalsApiMixin {

    @Shadow
    private static IntegerProperty FLOWER_AMOUNT;

    @Unique
    public int getMinimumFlowerAmount() {
        return Collections.min(FLOWER_AMOUNT.getPossibleValues());
    }
}
