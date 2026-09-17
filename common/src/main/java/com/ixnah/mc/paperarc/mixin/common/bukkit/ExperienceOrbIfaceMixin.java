package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.entity.ExperienceOrb;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.ExperienceOrb} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.ExperienceOrb", remap = false)
public interface ExperienceOrbIfaceMixin {

    @Unique
    public abstract int getCount();

    @Unique
    public abstract void setCount(int p0);

    @Unique
    public abstract java.util.UUID getTriggerEntityId();

    @Unique
    public abstract java.util.UUID getSourceEntityId();

    @Unique
    public default boolean isFromBottle() {
        ExperienceOrb self = (ExperienceOrb) this;
        return self.getSpawnReason() == ExperienceOrb.SpawnReason.EXP_BOTTLE;
    }
}
