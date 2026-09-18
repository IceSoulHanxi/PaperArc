package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.Translatable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code Translatable} 抽象方法叫 {@code translationKey()}，
 * 运行时（spigot-api）叫 {@code getTranslationKey()}。
 *
 * <p>一条 default 就把所有 Translatable 实现（PotionEffectType / Enchantment /
 * Material / EntityType / Villager.Profession / …）的 {@code translationKey()}
 * 兜住了；哪个类型自己另有实现，类方法优先级高于接口 default，语义不变
 * （checklist §1.10 am：X-2 第 ⑤ 批里 PotionEffectTypeWrapper 的转发把这个洞暴露出来）。</p>
 */
@Mixin(Translatable.class)
public interface TranslatableIfaceMixin {

    @Unique
    default String translationKey() {
        return ((Translatable) this).getTranslationKey();
    }
}
