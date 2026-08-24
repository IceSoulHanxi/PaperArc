package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AbstractHorse} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.AbstractHorse", remap = false)
public interface AbstractHorseIfaceMixin {

    @Unique
    public abstract org.bukkit.entity.Horse.Variant getVariant();

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
