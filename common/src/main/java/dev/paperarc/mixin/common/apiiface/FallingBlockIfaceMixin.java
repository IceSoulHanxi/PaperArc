package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.FallingBlock} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.FallingBlock", remap = false)
public interface FallingBlockIfaceMixin {

    public abstract void setBlockData(org.bukkit.block.data.BlockData p0);

    public abstract org.bukkit.block.BlockState getBlockState();

    public abstract void setBlockState(org.bukkit.block.BlockState p0);

    public abstract boolean doesAutoExpire();

    public abstract void shouldAutoExpire(boolean p0);
}
