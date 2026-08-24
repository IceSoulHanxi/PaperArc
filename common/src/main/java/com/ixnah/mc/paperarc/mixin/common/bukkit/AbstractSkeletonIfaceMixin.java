package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AbstractSkeleton} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.AbstractSkeleton", remap = false)
public interface AbstractSkeletonIfaceMixin {

    @Unique
    public abstract org.bukkit.entity.Skeleton.SkeletonType getSkeletonType();

    @Unique
    public abstract boolean shouldBurnInDay();

    @Unique
    public abstract void setShouldBurnInDay(boolean p0);
}
