package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AbstractSkeleton} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 *
 * <p>getSkeletonType 运行时接口上本来就有，@Unique 声明会被 Mixin 丢弃，已删（A4-1 s）。
 */
@Mixin(targets = "org.bukkit.entity.AbstractSkeleton", remap = false)
public interface AbstractSkeletonIfaceMixin {

    @Unique
    public abstract boolean shouldBurnInDay();

    @Unique
    public abstract void setShouldBurnInDay(boolean p0);
}
