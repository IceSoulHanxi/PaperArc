package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Fox} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Fox", remap = false)
public interface FoxIfaceMixin {

    public abstract void setInterested(boolean p0);

    public abstract boolean isInterested();

    public abstract void setLeaping(boolean p0);

    public abstract boolean isLeaping();

    public abstract void setDefending(boolean p0);

    public abstract boolean isDefending();

    public abstract void setFaceplanted(boolean p0);
}
