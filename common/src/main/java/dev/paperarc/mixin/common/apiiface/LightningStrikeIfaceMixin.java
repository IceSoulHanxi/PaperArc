package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.LightningStrike} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.LightningStrike", remap = false)
public interface LightningStrikeIfaceMixin {

    public abstract int getFlashCount();

    public abstract void setFlashCount(int p0);

    public abstract org.bukkit.entity.Entity getCausingEntity();
}
