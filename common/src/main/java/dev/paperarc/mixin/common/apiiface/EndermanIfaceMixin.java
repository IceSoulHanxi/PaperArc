package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Enderman} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Enderman", remap = false)
public interface EndermanIfaceMixin {

    public abstract boolean teleportRandomly();

    public abstract boolean isScreaming();

    public abstract void setScreaming(boolean p0);

    public abstract boolean hasBeenStaredAt();

    public abstract void setHasBeenStaredAt(boolean p0);
}
