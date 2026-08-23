package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Wither} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Wither", remap = false)
public interface WitherIfaceMixin {

    public abstract boolean isCharged();

    public abstract int getInvulnerableTicks();

    public abstract void setInvulnerableTicks(int p0);

    public abstract boolean canTravelThroughPortals();

    public abstract void setCanTravelThroughPortals(boolean p0);

    public abstract void enterInvulnerabilityPhase();
}
