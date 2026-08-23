package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Panda} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Panda", remap = false)
public interface PandaIfaceMixin {

    @Unique
    public abstract void setSneezeTicks(int p0);

    @Unique
    public abstract int getSneezeTicks();

    @Unique
    public abstract void setEatingTicks(int p0);

    @Unique
    public abstract int getEatingTicks();

    @Unique
    public abstract void setUnhappyTicks(int p0);

    @Unique
    public abstract org.bukkit.entity.Panda.Gene getCombinedGene();
}
