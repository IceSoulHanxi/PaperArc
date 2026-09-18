package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.enchantments.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code Enchantment} 实现 adventure {@code Translatable}（A6/X-2 第三批）。
 *
 * <p>paper 只在这个抽象类上写了 {@code implements}，{@code translationKey()} 由
 * {@code CraftEnchantment} 之类的具体类提供。运行时那些具体类也没有，所以这里把
 * {@code translationKey()} 直接做成基类上的**具体**方法 —— 一次覆盖全部子类
 * （含 {@code EnchantmentWrapper} 与插件自定义的附魔），不必逐个具体类补，
 * 也不会留下 {@code AbstractMethodError}。
 *
 * <p>取值与 vanilla 一致：附魔的 descriptionId 就是 {@code enchantment.<namespace>.<key>}。
 */
@Mixin(Enchantment.class)
public abstract class EnchantmentApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        org.bukkit.NamespacedKey key = ((Enchantment) (Object) this).getKey();
        return "enchantment." + key.getNamespace() + "." + key.getKey();
    }
}
