package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Bee} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Bee", remap = false)
public interface BeeIfaceMixin {

    public abstract void setRollingOverride(net.kyori.adventure.util.TriState p0);

    public abstract net.kyori.adventure.util.TriState getRollingOverride();

    public abstract boolean isRolling();

    public abstract void setCropsGrownSincePollination(int p0);

    public abstract int getCropsGrownSincePollination();

    public abstract void setTicksSincePollination(int p0);

    public abstract int getTicksSincePollination();
}
