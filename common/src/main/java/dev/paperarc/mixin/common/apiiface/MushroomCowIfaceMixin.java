package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.MushroomCow} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.MushroomCow", remap = false)
public interface MushroomCowIfaceMixin {

    @Unique
    public abstract boolean addEffectToNextStew(io.papermc.paper.potion.SuspiciousEffectEntry p0, boolean p1);

    @Unique
    public abstract java.util.List getStewEffects();

    @Unique
    public abstract void setStewEffects(java.util.List p0);
}
