package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.ExperienceOrb} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.ExperienceOrb", remap = false)
public interface ExperienceOrbIfaceMixin {

    public abstract int getCount();

    public abstract void setCount(int p0);

    public abstract java.util.UUID getTriggerEntityId();

    public abstract java.util.UUID getSourceEntityId();

    public abstract org.bukkit.entity.ExperienceOrb.SpawnReason getSpawnReason();
}
