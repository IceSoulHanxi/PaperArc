package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.potion.PotionBrewer}。
 * 实现体在 {@code bridge/PaperarcPotionBrewer}（Arclight 1.21.1 没有 CraftPotionBrewer，
 * 我们自己的纯内存实现就是 Server#getPotionBrewer() 的返回值）。
 */
@Mixin(targets = "org.bukkit.potion.PotionBrewer", remap = false)
public interface PotionBrewerIfaceMixin {

    @Unique
    public abstract void addPotionMix(io.papermc.paper.potion.PotionMix p0);

    @Unique
    public abstract void removePotionMix(org.bukkit.NamespacedKey p0);

    @Unique
    public abstract void resetPotionMixes();
}
