package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Mob} (generated, trimmed for 1.20.1).
 * Adds 10 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Mob", remap = false)
public interface MobIfaceMixin {

    @Unique
    public abstract com.destroystokyo.paper.entity.Pathfinder getPathfinder();

    @Unique
    public abstract boolean isInDaylight();

    @Unique
    public abstract void lookAt(org.bukkit.entity.Entity p0);

    @Unique
    public abstract void lookAt(double p0, double p1, double p2);

    @Unique
    public abstract void lookAt(double p0, double p1, double p2, float p3, float p4);

    @Unique
    public abstract int getHeadRotationSpeed();

    @Unique
    public abstract int getMaxHeadPitch();

    @Unique
    public abstract boolean isLeftHanded();

    @Unique
    public abstract void setLeftHanded(boolean p0);

    @Unique
    public abstract int getPossibleExperienceReward();
}
