package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.MushroomCow} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code io.papermc.paper.entity.Shearable}。终端方法 readyToBeSheared/shear 实现在 CraftShearableApiMixin 上。</p>
 */
@Mixin(targets = "org.bukkit.entity.MushroomCow", remap = false)
public interface MushroomCowIfaceMixin extends io.papermc.paper.entity.Shearable {

    @Unique
    public abstract int getStewEffectDuration();

    @Unique
    public abstract org.bukkit.potion.PotionEffectType getStewEffectType();

    @Unique
    public abstract void setStewEffect(org.bukkit.potion.PotionEffectType p0);

    @Unique
    public abstract void setStewEffectDuration(int p0);

}