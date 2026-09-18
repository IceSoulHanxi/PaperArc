package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PotionEffectHiddenBridge;
import org.bukkit.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code PotionEffect#getHiddenPotionEffect()}（gaps.md §3.1 事件方法表最后一行）。
 * NMS 的 {@code MobEffectInstance.hiddenEffect} 本来就有，只是 CraftBukkit 转 Bukkit 时丢了；
 * 填值在 {@code api.CraftPotionUtilHiddenMixin}。
 */
@Mixin(PotionEffect.class)
public abstract class PotionEffectHiddenMixin implements PotionEffectHiddenBridge {

    @Unique
    private PotionEffect paperarc$hiddenEffect;

    @Override
    public PotionEffect paperarc$getHiddenPotionEffect() {
        return this.paperarc$hiddenEffect;
    }

    @Override
    public void paperarc$setHiddenPotionEffect(PotionEffect hidden) {
        this.paperarc$hiddenEffect = hidden;
    }

    @Unique
    public PotionEffect getHiddenPotionEffect() {
        return this.paperarc$hiddenEffect;
    }
}
