package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.data.type.Leaves} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.data.type.Leaves", remap = false)
public interface LeavesIfaceMixin {

    @Unique
    public abstract int getMaximumDistance();

    @Unique
    public abstract int getMinimumDistance();
}
