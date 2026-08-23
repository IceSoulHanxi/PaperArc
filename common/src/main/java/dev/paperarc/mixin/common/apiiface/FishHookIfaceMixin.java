package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.FishHook} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.FishHook", remap = false)
public interface FishHookIfaceMixin {

    public abstract int getWaitTime();

    public abstract void setWaitTime(int p0);

    public abstract int getTimeUntilBite();

    public abstract void resetFishingState();
}
