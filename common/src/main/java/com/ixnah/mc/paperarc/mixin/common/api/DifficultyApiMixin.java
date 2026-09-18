package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 让 {@code Difficulty} 实现 adventure 的 {@code Translatable}（checklist §1.12 bb）。
 *
 * <p>原先靠 {@code TranslatableIfaceMixin} 给 {@code org.bukkit.Translatable} 加一条
 * default 兜底，但 paper 的 {@code org.bukkit.Translatable} 上**根本没有**
 * {@code translationKey()}（B6-1 的 {@code checkApiDescriptors} 抓出来的），而且
 * {@code Difficulty} 运行时连 {@code org.bukkit.Translatable} 都没实现，兜不到。
 *
 * <p>键值照抄 paper 的实现（{@code javap -c org/bukkit/Difficulty.class}：
 * {@code "options.difficulty." + name().toLowerCase(Locale.ENGLISH)}）。
 */
@Mixin(Difficulty.class)
public abstract class DifficultyApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "options.difficulty." + ((Difficulty) (Object) this).name().toLowerCase(Locale.ENGLISH);
    }
}
