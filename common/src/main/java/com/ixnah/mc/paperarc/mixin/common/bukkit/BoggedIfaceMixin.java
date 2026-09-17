package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.entity.Bogged} 补上 paper 声明的父接口 {@code io.papermc.paper.entity.Shearable}。
 */
@Mixin(targets = "org.bukkit.entity.Bogged", remap = false)
public interface BoggedIfaceMixin extends io.papermc.paper.entity.Shearable {

    /** paper {@code PaperShearable}（B3-2），实现体在 {@code bridge.api.PaperarcEntityTraits}。 */
    @Unique
    public default boolean readyToBeSheared() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.readyToBeSheared(this);
    }

    @Unique
    public default void shear(net.kyori.adventure.sound.Sound.Source source) {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.shear(this, source);
    }
}
