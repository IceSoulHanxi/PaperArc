package com.ixnah.mc.paperarc.bridge.api;

import com.ixnah.mc.paperarc.bridge.PotionEffectHiddenBridge;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;

/**
 * 给 CraftBukkit 造出来的 {@code PotionEffect} 补上 paper 的
 * {@code getHiddenPotionEffect()}（被更强的同类效果盖住的那一层）。
 *
 * <p>{@code CraftLivingEntity#getPotionEffect/getActivePotionEffects} 是**内联** new 的
 * {@code PotionEffect}（不经 {@code CraftPotionUtil#toBukkit}），所以光钩 toBukkit 不够 ——
 * 探针 P31 当场逼出来的。这里按效果类型回查 NMS 实例再挂上去。
 */
public final class PaperarcPotionEffects {

    private PaperarcPotionEffects() {
    }

    public static void attachHidden(net.minecraft.world.entity.LivingEntity handle, PotionEffect effect) {
        if (handle == null || effect == null) {
            return;
        }
        Holder<MobEffect> holder = CraftPotionEffectType.bukkitToMinecraftHolder(effect.getType());
        if (holder == null) {
            return;
        }
        MobEffectInstance nms = handle.getEffect(holder);
        if (nms == null || nms.hiddenEffect == null) {
            return;
        }
        // toBukkit 那条路已经由 CraftPotionUtilHiddenMixin 递归挂好整条链
        ((PotionEffectHiddenBridge) effect).paperarc$setHiddenPotionEffect(
                CraftPotionUtil.toBukkit(nms.hiddenEffect));
    }

    public static void attachHidden(net.minecraft.world.entity.LivingEntity handle,
                                    java.util.Collection<PotionEffect> effects) {
        if (effects == null) {
            return;
        }
        for (PotionEffect effect : effects) {
            attachHidden(handle, effect);
        }
    }
}
