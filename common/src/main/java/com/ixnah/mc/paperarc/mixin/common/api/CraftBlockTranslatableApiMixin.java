package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * B2-6：{@code Block extends Translatable} 的终端方法。
 * CraftBlock 已经有 Spigot 的 {@code getTranslationKey()}，adventure 要的是 {@code translationKey()}。
 */
@Mixin(CraftBlock.class)
public abstract class CraftBlockTranslatableApiMixin {

    @Shadow
    public abstract String getTranslationKey();

    @Unique
    public String translationKey() {
        return this.getTranslationKey();
    }
}
