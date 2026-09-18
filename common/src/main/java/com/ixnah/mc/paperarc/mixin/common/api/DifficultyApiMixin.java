package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 让 {@code Difficulty} 实现 adventure 的 {@code Translatable}（A6/X-2 第三批）。
 * 运行时这些枚举都没实现该接口，插件把它们当 Translatable 用就是 ClassCastException；
 * {@code translationKey()} 缺了则是 NoSuchMethodError。字符串拼法照抄 paper。
 */
@Mixin(Difficulty.class)
public abstract class DifficultyApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "options.difficulty." + ((Difficulty) (Object) this).name().toLowerCase(Locale.ENGLISH);
    }
}
