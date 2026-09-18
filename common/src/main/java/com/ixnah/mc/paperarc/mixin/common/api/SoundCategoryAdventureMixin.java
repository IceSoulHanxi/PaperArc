package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.sound.Sound;
import org.bukkit.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code org.bukkit.SoundCategory} 实现 adventure 的
 * {@code Sound.Source.Provider}。两边的常量名一一对应（MASTER/MUSIC/RECORDS/WEATHER/
 * BLOCKS/HOSTILE/NEUTRAL/PLAYERS/AMBIENT/VOICE），按名字转。
 */
@Mixin(SoundCategory.class)
public abstract class SoundCategoryAdventureMixin implements Sound.Source.Provider {

    @Unique
    public Sound.Source soundSource() {
        return Sound.Source.valueOf(((SoundCategory) (Object) this).name());
    }
}
