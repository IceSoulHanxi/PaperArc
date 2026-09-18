package com.ixnah.mc.paperarc.bridge.craft;

import com.destroystokyo.paper.Namespaced;

import java.util.Collection;
import java.util.Set;

/**
 * 把 {@code CraftMetaItem} 上 Paper 的 CanPlaceOn / CanDestroy 两组键暴露出来，
 * 好让 {@code CraftItemStack} 侧的落盘 mixin 读写它们（gaps.md G.1）。
 */
public interface CraftMetaItemAdventureBridge {

    Set<Namespaced> paperarc$placeableKeys();

    Set<Namespaced> paperarc$destroyableKeys();

    void paperarc$setPlaceableKeys(Collection<Namespaced> keys);

    void paperarc$setDestroyableKeys(Collection<Namespaced> keys);
}
