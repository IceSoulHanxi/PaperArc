package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.attribute.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code Attribute} 实现 adventure 的 {@code Translatable}（checklist §1.12 bb）。
 * paper 的 {@code translationKey()} 与 {@code getTranslationKey()} 是同一段字节码
 * （都走 {@code Bukkit.getUnsafe().getTranslationKey(attribute)}），运行时已有
 * {@code getTranslationKey()}，直接转调。
 */
@Mixin(Attribute.class)
public abstract class AttributeApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return ((Attribute) (Object) this).getTranslationKey();
    }
}
