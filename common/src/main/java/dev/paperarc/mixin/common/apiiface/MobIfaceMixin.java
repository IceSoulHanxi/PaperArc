package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Mob} (generated).
 * Adds 12 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Mob", remap = false)
public interface MobIfaceMixin {

    public abstract com.destroystokyo.paper.entity.Pathfinder getPathfinder();

    public abstract boolean isInDaylight();

    public abstract void lookAt(org.bukkit.Location p0);

    public abstract void lookAt(org.bukkit.Location p0, float p1, float p2);

    public abstract void lookAt(double p0, double p1, double p2, float p3, float p4);

    public abstract int getHeadRotationSpeed();

    public abstract int getMaxHeadPitch();

    public abstract boolean isAggressive();

    public abstract void setAggressive(boolean p0);

    public abstract boolean isLeftHanded();

    public abstract void setLeftHanded(boolean p0);

    public abstract int getPossibleExperienceReward();
}
