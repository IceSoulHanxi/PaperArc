package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Phantom} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Phantom", remap = false)
public interface PhantomIfaceMixin {

    @Unique
    public abstract java.util.UUID getSpawningEntity();

    @Unique
    public abstract boolean shouldBurnInDay();

    @Unique
    public abstract void setShouldBurnInDay(boolean p0);

    @Unique
    public abstract org.bukkit.Location getAnchorLocation();

    @Unique
    public abstract void setAnchorLocation(org.bukkit.Location p0);
}
