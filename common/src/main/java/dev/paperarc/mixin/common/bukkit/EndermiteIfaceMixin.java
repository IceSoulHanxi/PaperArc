package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Endermite} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Endermite", remap = false)
public interface EndermiteIfaceMixin {

    @Unique
    public abstract void setLifetimeTicks(int p0);

    @Unique
    public abstract int getLifetimeTicks();
}
