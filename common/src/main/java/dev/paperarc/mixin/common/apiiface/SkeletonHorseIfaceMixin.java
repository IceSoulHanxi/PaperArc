package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.SkeletonHorse} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.SkeletonHorse", remap = false)
public interface SkeletonHorseIfaceMixin {

    @Unique
    public abstract boolean isTrap();

    @Unique
    public abstract void setTrap(boolean p0);
}
