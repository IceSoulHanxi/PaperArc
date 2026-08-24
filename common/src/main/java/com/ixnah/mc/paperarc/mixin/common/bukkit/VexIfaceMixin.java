package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Vex} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Vex", remap = false)
public interface VexIfaceMixin {

    @Unique
    public abstract org.bukkit.entity.Mob getSummoner();

    @Unique
    public abstract void setSummoner(org.bukkit.entity.Mob p0);

    @Unique
    public abstract boolean hasLimitedLifetime();

    @Unique
    public abstract void setLimitedLifetime(boolean p0);

    @Unique
    public abstract int getLimitedLifetimeTicks();

    @Unique
    public abstract void setLimitedLifetimeTicks(int p0);
}
