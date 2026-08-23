package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Ravager} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Ravager", remap = false)
public interface RavagerIfaceMixin {

    public abstract int getAttackTicks();

    public abstract void setAttackTicks(int p0);

    public abstract int getStunnedTicks();

    public abstract void setStunnedTicks(int p0);

    public abstract int getRoarTicks();

    public abstract void setRoarTicks(int p0);
}
