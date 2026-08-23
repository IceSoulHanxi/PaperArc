package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Block} (generated).
 * Adds 15 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Block", remap = false)
public interface BlockIfaceMixin {

    public abstract boolean isValidTool(org.bukkit.inventory.ItemStack p0);

    public abstract org.bukkit.block.BlockState getState(boolean p0);

    public abstract org.bukkit.block.Biome getComputedBiome();

    public abstract boolean isBuildable();

    public abstract boolean isBurnable();

    public abstract boolean isReplaceable();

    public abstract boolean isSolid();

    public abstract boolean isCollidable();

    public abstract boolean breakNaturally(boolean p0, boolean p1);

    public abstract boolean breakNaturally(org.bukkit.inventory.ItemStack p0, boolean p1, boolean p2);

    public abstract void tick();

    public abstract void fluidTick();

    public abstract void randomTick();

    public abstract com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup();

    public abstract org.bukkit.SoundGroup getBlockSoundGroup();
}
