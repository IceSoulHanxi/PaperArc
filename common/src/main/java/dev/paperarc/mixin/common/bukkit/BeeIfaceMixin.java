package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Bee} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Bee", remap = false)
public interface BeeIfaceMixin {

    @Unique
    public abstract void setRollingOverride(net.kyori.adventure.util.TriState p0);

    @Unique
    public abstract net.kyori.adventure.util.TriState getRollingOverride();

    @Unique
    public abstract boolean isRolling();

    @Unique
    public abstract void setCropsGrownSincePollination(int p0);

    @Unique
    public abstract int getCropsGrownSincePollination();

    @Unique
    public abstract void setTicksSincePollination(int p0);

    @Unique
    public abstract int getTicksSincePollination();
}
