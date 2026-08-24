package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Block} (generated).
 * Adds 15 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Block", remap = false)
public interface BlockIfaceMixin {

    @Unique
    public abstract boolean isValidTool(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract org.bukkit.block.BlockState getState(boolean p0);

    @Unique
    public abstract org.bukkit.block.Biome getComputedBiome();

    @Unique
    public abstract boolean isBuildable();

    @Unique
    public abstract boolean isBurnable();

    @Unique
    public abstract boolean isReplaceable();

    @Unique
    public abstract boolean isSolid();

    @Unique
    public abstract boolean isCollidable();

    @Unique
    public abstract boolean breakNaturally(boolean p0, boolean p1);

    @Unique
    public abstract boolean breakNaturally(org.bukkit.inventory.ItemStack p0, boolean p1, boolean p2);

    @Unique
    public abstract void tick();

    @Unique
    public abstract void fluidTick();

    @Unique
    public abstract void randomTick();

    @Unique
    public abstract com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup();

    @Unique
    public abstract org.bukkit.SoundGroup getBlockSoundGroup();
}
