package com.ixnah.mc.paperarc.mixin.common.bukkit;

import net.kyori.adventure.translation.Translatable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code Attribute} 实现 adventure 的 {@code Translatable}（checklist §1.12 bb）。
 * paper 的 {@code translationKey()} 与 {@code getTranslationKey()} 是同一段字节码
 * （都走 {@code Bukkit.getUnsafe().getTranslationKey(attribute)}），运行时已有
 * {@code getTranslationKey()}，直接转调。
 *
 * <p>1.21.11 起运行时的 {@code Attribute} 是接口（{@code OldEnum}），改成 interface mixin。
 */
@Mixin(targets = "org.bukkit.attribute.Attribute", remap = false)
public interface AttributeIfaceMixin extends Translatable {

    @Unique
    public default String translationKey() {
        return ((org.bukkit.attribute.Attribute) this).getTranslationKey();
    }
}
