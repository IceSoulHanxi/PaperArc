package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.BlockState} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.BlockState", remap = false)
public interface BlockStateIfaceMixin {

    @Unique
    public abstract boolean isCollidable();

    @Unique
    public abstract java.util.Collection getDrops(org.bukkit.inventory.ItemStack p0, org.bukkit.entity.Entity p1);
}
