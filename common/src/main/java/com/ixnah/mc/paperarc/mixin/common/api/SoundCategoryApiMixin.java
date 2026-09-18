package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code SoundCategory} 实现 adventure 的 {@code Sound.Source.Provider}
 * （A6/X-2 第三批）。两边枚举名一一对应，按名字转换。
 */
@Mixin(SoundCategory.class)
public abstract class SoundCategoryApiMixin implements net.kyori.adventure.sound.Sound.Source.Provider {

    @Unique
    public net.kyori.adventure.sound.Sound.Source soundSource() {
        return net.kyori.adventure.sound.Sound.Source.valueOf(((SoundCategory) (Object) this).name());
    }
}
