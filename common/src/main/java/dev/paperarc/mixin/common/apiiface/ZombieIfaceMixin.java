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

    @Unique
    public abstract boolean isDrowning();

    @Unique
    public abstract void startDrowning(int p0);

    @Unique
    public abstract void stopDrowning();

    @Unique
    public abstract void setArmsRaised(boolean p0);

    @Unique
    public abstract boolean isArmsRaised();

    @Unique
    public abstract boolean shouldBurnInDay();

    @Unique
    public abstract void setShouldBurnInDay(boolean p0);

    @Unique
    public abstract boolean supportsBreakingDoors();
}
