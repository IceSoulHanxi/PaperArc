package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B2-6：给 {@link org.bukkit.block.Container} 补上 paper 声明的父接口 {@code io.papermc.paper.block.LockableTileState, io.papermc.paper.block.TileStateInventoryHolder}。
 * 两个父接口自身没有新的抽象方法：LockableTileState 的方法来自 Lockable/TileState（运行时都有），TileStateInventoryHolder 的 getInventory/getSnapshotInventory 也是 Container 本来就有的，所以只补继承关系。
 */
@Mixin(targets = "org.bukkit.block.Container", remap = false)
public interface ContainerSuperIfaceMixin extends io.papermc.paper.block.LockableTileState, io.papermc.paper.block.TileStateInventoryHolder {
}
