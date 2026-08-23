package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AreaEffectCloud} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.AreaEffectCloud", remap = false)
public interface AreaEffectCloudIfaceMixin {

    public abstract java.util.UUID getOwnerUniqueId();

    public abstract void setOwnerUniqueId(java.util.UUID p0);
}
