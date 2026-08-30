package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v1_20_R1.block.CraftBrewingStand;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Reserved for Paper brewing additions on {@link CraftBrewingStand}.
 *
 * <p>paper-api 1.20.1's {@code BrewingStand} interface only exposes
 * {@code getBrewingTime()}/{@code setBrewingTime(int)} and
 * {@code getFuelLevel()}/{@code setFuelLevel(int)} — the 1.21-era
 * {@code getRecipeBrewTime()}/{@code setRecipeBrewTime(int)} pair is not part
 * of the 1.20.1 API surface, so no supplementary methods are needed here.</p>
 */
@Mixin(CraftBrewingStand.class)
public abstract class CraftBrewingStandApiMixin {
}
