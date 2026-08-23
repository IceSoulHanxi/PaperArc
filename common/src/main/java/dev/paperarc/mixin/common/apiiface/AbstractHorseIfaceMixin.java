package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AbstractHorse} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.AbstractHorse", remap = false)
public interface AbstractHorseIfaceMixin {

    public abstract org.bukkit.entity.Horse.Variant getVariant();

    public abstract boolean isEatingGrass();

    public abstract void setEatingGrass(boolean p0);

    public abstract boolean isRearing();

    public abstract void setRearing(boolean p0);

    public abstract boolean isEating();

    public abstract void setEating(boolean p0);
}
