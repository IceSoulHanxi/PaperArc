package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.ShulkerBullet} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.ShulkerBullet", remap = false)
public interface ShulkerBulletIfaceMixin {

    public abstract org.bukkit.util.Vector getTargetDelta();

    public abstract void setTargetDelta(org.bukkit.util.Vector p0);

    public abstract org.bukkit.block.BlockFace getCurrentMovementDirection();

    public abstract void setCurrentMovementDirection(org.bukkit.block.BlockFace p0);

    public abstract int getFlightSteps();

    public abstract void setFlightSteps(int p0);
}
