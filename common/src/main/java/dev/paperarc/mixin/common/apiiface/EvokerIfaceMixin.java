package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Evoker} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Evoker", remap = false)
public interface EvokerIfaceMixin {

    public abstract org.bukkit.entity.Sheep getWololoTarget();

    public abstract void setWololoTarget(org.bukkit.entity.Sheep p0);
}
