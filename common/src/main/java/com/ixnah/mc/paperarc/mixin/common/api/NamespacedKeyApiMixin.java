package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.Namespaced;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 让 {@code org.bukkit.NamespacedKey} 实现 paper 给它加的两个接口
 * （adventure {@code Key} 与 {@code com.destroystokyo.paper.Namespaced}）并补三个方法。
 *
 * <p>A6/X-2 实测：不补的话，任何形参/返回是 {@code Namespaced} 的 paper API
 * （{@code ItemMeta#setPlaceableKeys}、{@code get/setDestroyableKeys}…）一旦碰上
 * {@code Material#getKey()} 这种运行时 {@code NamespacedKey} 就是
 * {@code ClassCastException: org.bukkit.NamespacedKey cannot be cast to
 * com.destroystokyo.paper.Namespaced}（探针 P14c 实测）。
 * {@code audit.py} 只比接口的父接口，具体类上的 implements 是第三个审计盲区
 * —— 全仓共 23 个类型缺，清单见 {@code docs/data/1201-class-method-gaps.md}。
 *
 * <p>{@code getNamespace()}/{@code getKey()}（Namespaced 的两个抽象方法）运行时已有，
 * 这里只补 {@code Key} 的 {@code namespace()/value()/asString()}；其余 {@code Key}
 * 成员都是 default。
 */
@Mixin(NamespacedKey.class)
public abstract class NamespacedKeyApiMixin implements Key, Namespaced {

    @Unique
    public String namespace() {
        return ((NamespacedKey) (Object) this).getNamespace();
    }

    @Unique
    public String value() {
        return ((NamespacedKey) (Object) this).getKey();
    }

    @Unique
    public String asString() {
        return ((NamespacedKey) (Object) this).toString();
    }
}
