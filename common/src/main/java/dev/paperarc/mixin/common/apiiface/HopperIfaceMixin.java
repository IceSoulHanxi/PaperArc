package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Hopper} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Hopper", remap = false)
public interface HopperIfaceMixin {

    @Unique
    public abstract void setTransferCooldown(int p0);

    @Unique
    public abstract int getTransferCooldown();
}
