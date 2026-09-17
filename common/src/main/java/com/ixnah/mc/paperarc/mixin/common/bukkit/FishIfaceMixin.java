package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.entity.Fish} 补上 paper 声明的父接口 {@code io.papermc.paper.entity.Bucketable}。
 */
@Mixin(targets = "org.bukkit.entity.Fish", remap = false)
public interface FishIfaceMixin extends io.papermc.paper.entity.Bucketable {

    /** paper {@code PaperBucketable}（B3-2），实现体在 {@code bridge.api.PaperarcEntityTraits}。 */
    @Unique
    public default boolean isFromBucket() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.isFromBucket(this);
    }

    @Unique
    public default void setFromBucket(boolean fromBucket) {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.setFromBucket(this, fromBucket);
    }

    @Unique
    public default org.bukkit.inventory.ItemStack getBaseBucketItem() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.getBaseBucketItem(this);
    }

    @Unique
    public default org.bukkit.Sound getPickupSound() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.getPickupSound(this);
    }
}
