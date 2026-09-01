package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Collections;

import org.bukkit.craftbukkit.v.block.data.CraftLevelled;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Adds getMinimumLevel missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Add-missing-block-data-API.patch (getMin(LEVEL)).
 */
@Mixin(CraftLevelled.class)
public abstract class CraftLevelledApiMixin {

    @Shadow
    private static IntegerProperty LEVEL;

    @Unique
    public int getMinimumLevel() {
        return Collections.min(LEVEL.getPossibleValues());
    }
}
