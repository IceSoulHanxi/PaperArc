package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Zombie} (generated).
 * Adds 8 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Zombie", remap = false)
public interface ZombieIfaceMixin {

    public abstract boolean isDrowning();

    public abstract void startDrowning(int p0);

    public abstract void stopDrowning();

    public abstract void setArmsRaised(boolean p0);

    public abstract boolean isArmsRaised();

    public abstract boolean shouldBurnInDay();

    public abstract void setShouldBurnInDay(boolean p0);

    public abstract boolean supportsBreakingDoors();
}
