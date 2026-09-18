package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcPotionEffects;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * 把 paper 的 {@code PotionEffect#getHiddenPotionEffect()} 接到 CraftBukkit 真正造
 * {@code PotionEffect} 的两个地方。{@code CraftLivingEntity} 是内联 new 的，
 * 不走 {@code CraftPotionUtil#toBukkit}（探针 P31 逼出）。
 */
@Mixin(CraftLivingEntity.class)
public abstract class CraftLivingEntityPotionHiddenMixin {

    @Inject(method = "getPotionEffect", at = @At("RETURN"), remap = false)
    private void paperarc$attachHiddenSingle(PotionEffectType type,
                                             CallbackInfoReturnable<PotionEffect> cir) {
        PaperarcPotionEffects.attachHidden(
                ((CraftLivingEntity) (Object) this).getHandle(), cir.getReturnValue());
    }

    @Inject(method = "getActivePotionEffects", at = @At("RETURN"), remap = false)
    private void paperarc$attachHiddenAll(CallbackInfoReturnable<Collection<PotionEffect>> cir) {
        PaperarcPotionEffects.attachHidden(
                ((CraftLivingEntity) (Object) this).getHandle(), cir.getReturnValue());
    }
}
