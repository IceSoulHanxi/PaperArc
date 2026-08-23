package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Dolphin} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Dolphin", remap = false)
public interface DolphinIfaceMixin {

    public abstract int getMoistness();

    public abstract void setMoistness(int p0);

    public abstract void setHasFish(boolean p0);

    public abstract boolean hasFish();

    public abstract org.bukkit.Location getTreasureLocation();

    public abstract void setTreasureLocation(org.bukkit.Location p0);
}
