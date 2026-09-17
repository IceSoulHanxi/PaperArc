package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B2-6：给 {@link org.bukkit.block.ChiseledBookshelf} 补上 paper 声明的父接口 {@code io.papermc.paper.block.TileStateInventoryHolder}。
 * 同上，只补继承关系。
 */
@Mixin(targets = "org.bukkit.block.ChiseledBookshelf", remap = false)
public interface ChiseledBookshelfIfaceMixin extends io.papermc.paper.block.TileStateInventoryHolder {
}
