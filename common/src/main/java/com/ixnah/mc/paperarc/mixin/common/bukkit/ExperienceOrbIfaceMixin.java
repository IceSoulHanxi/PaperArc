package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.ExperienceOrb} (generated, trimmed for 1.20.1).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.ExperienceOrb", remap = false)
public interface ExperienceOrbIfaceMixin {

    @Unique
    public abstract java.util.UUID getTriggerEntityId();

    @Unique
    public abstract java.util.UUID getSourceEntityId();

    @Unique
    public abstract org.bukkit.entity.ExperienceOrb.SpawnReason getSpawnReason();
}
