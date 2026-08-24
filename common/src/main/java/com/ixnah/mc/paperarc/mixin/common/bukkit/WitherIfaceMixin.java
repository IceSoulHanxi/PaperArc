package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Wither} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Wither", remap = false)
public interface WitherIfaceMixin {

    @Unique
    public abstract boolean isCharged();

    @Unique
    public abstract int getInvulnerableTicks();

    @Unique
    public abstract void setInvulnerableTicks(int p0);

    @Unique
    public abstract boolean canTravelThroughPortals();

    @Unique
    public abstract void setCanTravelThroughPortals(boolean p0);

    @Unique
    public abstract void enterInvulnerabilityPhase();
}
