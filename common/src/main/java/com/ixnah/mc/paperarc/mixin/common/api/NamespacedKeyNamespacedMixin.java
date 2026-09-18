package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.Namespaced;
import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 让 {@code org.bukkit.NamespacedKey} 实现 paper 的
 * {@code com.destroystokyo.paper.Namespaced}（paper-api 里就是这么声明的，
 * Arclight 的 spigot-api 没有）。
 *
 * <p>不补会怎样：{@code CraftMetaItem} 的 CanPlaceOn/CanDestroy 一族方法签名是
 * {@code Collection<Namespaced>}，方法体里的 for-each 会编译出
 * {@code checkcast com/destroystokyo/paper/Namespaced}；插件塞进来的
 * {@code NamespacedKey}（{@code Material#getKey()} 的返回值）在运行时不实现该接口，
 * 一取就 {@code ClassCastException}。这类"paper 给 org.bukkit 具体类加父接口"的缺口
 * {@code audit.py --hierarchy} 照不到（它只比对接口对接口），同 {@code Location}
 * 实现 {@code FinePosition} 那条（§1.8 ab）。</p>
 *
 * <p>两个抽象方法 {@code getNamespace()}/{@code getKey()} 运行时的
 * {@code NamespacedKey} 本来就有，所以本 mixin 不需要任何成员。</p>
 */
@Mixin(NamespacedKey.class)
public abstract class NamespacedKeyNamespacedMixin implements Namespaced {
}
