package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.persistence.PersistentDataHolder} 补上 paper 声明的父接口 {@code io.papermc.paper.persistence.PersistentDataViewHolder}。
 */
@Mixin(targets = "org.bukkit.persistence.PersistentDataHolder", remap = false)
public interface PersistentDataHolderIfaceMixin extends io.papermc.paper.persistence.PersistentDataViewHolder {
}
