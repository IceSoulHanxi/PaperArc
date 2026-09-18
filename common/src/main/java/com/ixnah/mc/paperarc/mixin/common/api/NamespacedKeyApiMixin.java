package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code NamespacedKey implements net.kyori.adventure.key.Key}
 * （checklist §1.10 am 第 ⑤ 批的 3 个方法 + §1.12 bb 的 implements 缺口）。
 *
 * <p>此前只补了 {@code namespace()/value()/asString()} 三个方法体、没补 {@code implements}，
 * 于是任何把 NamespacedKey 当 Key 用的地方都是 {@code IncompatibleClassChangeError} ——
 * B6-1 新补的 {@code Server#getWorld(NamespacedKey)}（paper 的 default 直接强转 Key 转调
 * {@code getWorld(Key)}）在探针 P25 上实测踩到。{@code Key} 的其余成员
 * （{@code asMinimalString/examinableProperties/compareTo/key}）在接口里都是 default。
 *
 * <p>paper 读私有字段，这里换公开 getter，取值一致。
 */
@Mixin(NamespacedKey.class)
public abstract class NamespacedKeyApiMixin implements net.kyori.adventure.key.Key {

    @Unique
    private NamespacedKey paperarc$self() {
        return (NamespacedKey) (Object) this;
    }

    @Unique
    public String namespace() {
        return this.paperarc$self().getNamespace();
    }

    @Unique
    public String value() {
        return this.paperarc$self().getKey();
    }

    @Unique
    public String asString() {
        NamespacedKey self = this.paperarc$self();
        return self.getNamespace() + ":" + self.getKey();
    }
}
