package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.entity.TropicalFish} 补上 paper 声明的父接口 {@code io.papermc.paper.entity.SchoolableFish}。
 */
@Mixin(targets = "org.bukkit.entity.TropicalFish", remap = false)
public interface TropicalFishIfaceMixin extends io.papermc.paper.entity.SchoolableFish {

    /** paper {@code SchoolableFish}（B3-2），实现体在 {@code bridge.api.PaperarcEntityTraits}。 */
    @Unique
    public default void startFollowing(io.papermc.paper.entity.SchoolableFish leader) {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.startFollowing(this, leader);
    }

    @Unique
    public default void stopFollowing() {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.stopFollowing(this);
    }

    @Unique
    public default int getSchoolSize() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.getSchoolSize(this);
    }

    @Unique
    public default int getMaxSchoolSize() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.getMaxSchoolSize(this);
    }

    @Unique
    public default io.papermc.paper.entity.SchoolableFish getSchoolLeader() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.getSchoolLeader(this);
    }
}
