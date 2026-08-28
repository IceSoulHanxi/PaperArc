package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v1_20_R1.block.CraftDecoratedPot;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder target for CraftDecoratedPot.
 *
 * 1.20.1 paper-api's {@link org.bukkit.block.data.type.DecoratedPot} is an empty
 * interface (no isCracked/setCracked — those are 1.21 additions), so there is no
 * missing API surface to implement here.
 */
@Mixin(CraftDecoratedPot.class)
public abstract class CraftDecoratedPotApiMixin {
}