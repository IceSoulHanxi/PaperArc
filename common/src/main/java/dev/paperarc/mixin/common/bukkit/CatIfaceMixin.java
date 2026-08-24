package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Cat} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Cat", remap = false)
public interface CatIfaceMixin {

    @Unique
    public abstract void setLyingDown(boolean p0);

    @Unique
    public abstract boolean isLyingDown();

    @Unique
    public abstract void setHeadUp(boolean p0);

    @Unique
    public abstract boolean isHeadUp();
}
