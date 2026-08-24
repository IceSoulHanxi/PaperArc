package com.ixnah.mc.paperarc.mixin.common.bukkit;

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

}
