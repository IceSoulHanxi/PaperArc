package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.meta.SpawnEggMeta} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.meta.SpawnEggMeta", remap = false)
public interface SpawnEggMetaIfaceMixin {

    @Unique
    public abstract org.bukkit.entity.EntityType getCustomSpawnedType();

    @Unique
    public abstract void setCustomSpawnedType(org.bukkit.entity.EntityType p0);

}