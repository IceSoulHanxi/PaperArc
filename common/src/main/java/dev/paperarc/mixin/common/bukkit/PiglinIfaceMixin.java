package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Piglin} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Piglin", remap = false)
public interface PiglinIfaceMixin {

    @Unique
    public abstract void setChargingCrossbow(boolean p0);

    @Unique
    public abstract boolean isChargingCrossbow();

    @Unique
    public abstract void setDancing(boolean p0);

    @Unique
    public abstract boolean isDancing();
}
