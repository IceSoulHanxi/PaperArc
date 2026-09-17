package com.ixnah.mc.paperarc.mixin.common.bukkit;

import net.kyori.adventure.key.Key;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.Keyed} 补上 paper 声明的父接口
 * {@code net.kyori.adventure.key.Keyed}。key() 用 default 方法体直接给出，覆盖全部注册表类型。
 */
@Mixin(targets = "org.bukkit.Keyed", remap = false)
public interface KeyedIfaceMixin extends net.kyori.adventure.key.Keyed {

    @Unique
    public default Key key() {
        org.bukkit.NamespacedKey key = ((org.bukkit.Keyed) this).getKey();
        return Key.key(key.getNamespace(), key.getKey());
    }
}
