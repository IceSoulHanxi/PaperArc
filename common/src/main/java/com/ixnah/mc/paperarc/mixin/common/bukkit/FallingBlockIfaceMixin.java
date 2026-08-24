package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.FallingBlock} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.FallingBlock", remap = false)
public interface FallingBlockIfaceMixin {

    @Unique
    public abstract void setBlockData(org.bukkit.block.data.BlockData p0);

    @Unique
    public abstract org.bukkit.block.BlockState getBlockState();

    @Unique
    public abstract void setBlockState(org.bukkit.block.BlockState p0);

    @Unique
    public abstract boolean doesAutoExpire();

    @Unique
    public abstract void shouldAutoExpire(boolean p0);
}
