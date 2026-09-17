package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.block.Container} 补上 paper 声明的父接口
 * {@code io.papermc.paper.block.LockableTileState}。父接口自身没有新的抽象方法（都来自 Lockable/TileState，运行时都有），只补继承关系。
 */
@Mixin(targets = "org.bukkit.block.Container", remap = false)
public interface ContainerIfaceMixin extends io.papermc.paper.block.LockableTileState {
}
