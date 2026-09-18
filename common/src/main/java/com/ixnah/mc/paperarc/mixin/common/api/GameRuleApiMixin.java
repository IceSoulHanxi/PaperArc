package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.GameRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code GameRule} 实现 adventure 的 {@code Translatable}（checklist §1.12 bb）。
 * 键值照抄 paper（{@code "gamerule." + name}），运行时用公开的 {@code getName()} 取同一个字段。
 */
@Mixin(GameRule.class)
public abstract class GameRuleApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "gamerule." + ((GameRule<?>) (Object) this).getName();
    }
}
