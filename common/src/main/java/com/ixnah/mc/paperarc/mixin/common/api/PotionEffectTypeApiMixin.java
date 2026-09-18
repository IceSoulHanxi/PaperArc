package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code PotionEffectType} 实现 adventure {@code Translatable}（A6/X-2 第三批）。
 * 同 {@link EnchantmentApiMixin}：{@code translationKey()} 做成基类具体方法，一次覆盖
 * 全部子类（含 {@code PotionEffectTypeWrapper}）。vanilla 的 descriptionId 形如
 * {@code effect.minecraft.speed}。
 */
@Mixin(PotionEffectType.class)
public abstract class PotionEffectTypeApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        org.bukkit.NamespacedKey key = ((PotionEffectType) (Object) this).getKey();
        return "effect." + key.getNamespace() + "." + key.getKey();
    }
}
