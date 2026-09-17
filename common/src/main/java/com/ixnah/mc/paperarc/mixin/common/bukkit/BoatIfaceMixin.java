package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Boat} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Boat", remap = false)
public interface BoatIfaceMixin extends io.papermc.paper.entity.Leashable {

    @Unique
    public abstract org.bukkit.Material getBoatMaterial();

    /** paper {@code Leashable}（B3-2）：1.21.1 的 NMS Boat 自己就实现了 {@code Leashable}。 */
    @Unique
    public default boolean isLeashed() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.isLeashed(this);
    }

    @Unique
    public default org.bukkit.entity.Entity getLeashHolder() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.getLeashHolder(this);
    }

    @Unique
    public default boolean setLeashHolder(org.bukkit.entity.Entity holder) {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.setLeashHolder(this, holder);
    }
}
