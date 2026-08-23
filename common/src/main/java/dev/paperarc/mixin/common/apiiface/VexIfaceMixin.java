package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Vex} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Vex", remap = false)
public interface VexIfaceMixin {

    public abstract org.bukkit.entity.Mob getSummoner();

    public abstract void setSummoner(org.bukkit.entity.Mob p0);

    public abstract boolean hasLimitedLifetime();

    public abstract void setLimitedLifetime(boolean p0);

    public abstract int getLimitedLifetimeTicks();

    public abstract void setLimitedLifetimeTicks(int p0);
}
