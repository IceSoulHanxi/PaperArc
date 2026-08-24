package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Enderman} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Enderman", remap = false)
public interface EndermanIfaceMixin {

    @Unique
    public abstract boolean teleportRandomly();

    @Unique
    public abstract boolean isScreaming();

    @Unique
    public abstract void setScreaming(boolean p0);

    @Unique
    public abstract boolean hasBeenStaredAt();

    @Unique
    public abstract void setHasBeenStaredAt(boolean p0);
}
