package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.GameRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code GameRule} 实现 adventure {@code Translatable}（A6/X-2 第三批）。
 * paper 读私有字段 {@code name}，这里走等价的公开 {@code getName()}。
 */
@Mixin(GameRule.class)
public abstract class GameRuleApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "gamerule." + ((GameRule<?>) (Object) this).getName();
    }
}
