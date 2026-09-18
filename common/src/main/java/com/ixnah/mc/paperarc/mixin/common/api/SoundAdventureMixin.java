package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code org.bukkit.Sound} 实现 adventure 的 {@code Sound.Type}
 *（gaps.md §0「implements 缺口」那一行）。不实现的话，所有形参是 {@code Sound.Type} 的
 * adventure API（{@code Sound.sound(Type, Source, …)}）插件传 Bukkit 的 Sound 进去就编译得过、
 * 运行时 {@code ClassCastException}。
 *
 * <p>{@code Sound.Type} 只有一个抽象方法 {@code key()}，值就是 Bukkit 的
 * {@code getKey()}（两边都是 {@code minecraft:entity.pig.ambient} 这种命名空间键）。
 */
@Mixin(org.bukkit.Sound.class)
public abstract class SoundAdventureMixin implements Sound.Type {

    @Unique
    public Key key() {
        org.bukkit.NamespacedKey key = ((org.bukkit.Sound) (Object) this).getKey();
        return Key.key(key.getNamespace(), key.getKey());
    }
}
