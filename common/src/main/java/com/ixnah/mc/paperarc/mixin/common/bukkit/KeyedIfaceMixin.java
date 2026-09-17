package com.ixnah.mc.paperarc.mixin.common.bukkit;

import net.kyori.adventure.key.Key;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B2-6：paper-api 的 {@code org.bukkit.Keyed extends net.kyori.adventure.key.Keyed}，
 * 并以 default 方法给出 {@code key()}。Arclight 运行时的 Keyed 两样都没有。
 *
 * <p>这一条的覆盖面最大 —— org.bukkit 里几乎所有注册表类型都是 Keyed，
 * 一条 default 方法就让它们全部能当 adventure 的 {@code Keyed} 用
 * （interface mixin 的父接口与 default 方法体都会合并进目标，A2-1 实测）。</p>
 */
@Mixin(targets = "org.bukkit.Keyed", remap = false)
public interface KeyedIfaceMixin extends net.kyori.adventure.key.Keyed {

    @Unique
    public default Key key() {
        org.bukkit.NamespacedKey key = ((org.bukkit.Keyed) this).getKey();
        return Key.key(key.getNamespace(), key.getKey());
    }
}
