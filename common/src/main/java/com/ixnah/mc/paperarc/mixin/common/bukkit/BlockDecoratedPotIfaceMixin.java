package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B2-6：给 {@link org.bukkit.block.DecoratedPot} 补上 paper 声明的父接口 {@code io.papermc.paper.block.TileStateInventoryHolder}。
 * getInventory/getSnapshotInventory 运行时已有，只补继承关系。注意与 DecoratedPotIfaceMixin（块数据接口）不是一回事。
 */
@Mixin(targets = "org.bukkit.block.DecoratedPot", remap = false)
public interface BlockDecoratedPotIfaceMixin extends io.papermc.paper.block.TileStateInventoryHolder {
}
