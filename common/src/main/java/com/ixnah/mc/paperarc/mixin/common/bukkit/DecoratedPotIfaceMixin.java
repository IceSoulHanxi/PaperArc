package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.data.type.DecoratedPot} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.data.type.DecoratedPot", remap = false)
public interface DecoratedPotIfaceMixin {

    @Unique
    public abstract boolean isCracked();

    @Unique
    public abstract void setCracked(boolean p0);
}
