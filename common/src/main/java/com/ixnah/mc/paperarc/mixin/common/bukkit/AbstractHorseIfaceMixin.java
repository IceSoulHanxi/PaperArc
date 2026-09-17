package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AbstractHorse} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 *
 * <p>getVariant 运行时接口上本来就有，@Unique 声明会被 Mixin 丢弃，已删（A4-1 s）。
 */
@Mixin(targets = "org.bukkit.entity.AbstractHorse", remap = false)
public interface AbstractHorseIfaceMixin {

    @Unique
    public abstract boolean isEatingGrass();

    @Unique
    public abstract void setEatingGrass(boolean p0);

    @Unique
    public abstract boolean isRearing();

    @Unique
    public abstract void setRearing(boolean p0);

    @Unique
    public abstract boolean isEating();

    @Unique
    public abstract void setEating(boolean p0);
}
