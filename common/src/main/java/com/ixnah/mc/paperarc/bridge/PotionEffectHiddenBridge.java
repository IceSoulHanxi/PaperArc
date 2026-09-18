package com.ixnah.mc.paperarc.bridge;

/**
 * Paper 给 {@code org.bukkit.potion.PotionEffect} 加的 {@code hiddenEffect}
 *（同名药水效果被更强的一层"盖住"时，底下那层）。运行时的 PotionEffect 没有这个状态，
 * 由 {@code api.PotionEffectHiddenMixin} 注入，{@code api.CraftPotionUtilHiddenMixin}
 * 在从 NMS 转 Bukkit 时逐层填。
 */
public interface PotionEffectHiddenBridge {

    org.bukkit.potion.PotionEffect paperarc$getHiddenPotionEffect();

    void paperarc$setHiddenPotionEffect(org.bukkit.potion.PotionEffect hidden);
}
