package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.key.Key;
import org.bukkit.Sound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code Sound} 实现 adventure 的 {@code Sound.Type}（A6/X-2 第三批），
 * 于是 {@code Audience#playSound(net.kyori.adventure.sound.Sound)} 能直接吃 Bukkit 的枚举。
 * {@code Sound.Type} 的抽象方法只有 {@code key()}，返回值用 Bukkit 自己的
 * {@code getKey()} —— {@code NamespacedKey} 已经由 NamespacedKeyApiMixin 实现了 {@code Key}。
 */
@Mixin(Sound.class)
public abstract class SoundApiMixin implements net.kyori.adventure.sound.Sound.Type {

    @Unique
    public Key key() {
        return ((Sound) (Object) this).getKey();
    }
}
