package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A5-1 父接口差集：给 {@link org.bukkit.entity.minecart.CommandMinecart} 补上 paper 声明的
 * 父接口 {@code io.papermc.paper.command.CommandBlockHolder}。
 *
 * <p>{@code getCommand}/{@code setCommand} 运行时本来就有（Bukkit 的 CommandMinecart
 * 自己就声明了），successCount 与 lastOutput 四条补在 {@code CraftMinecartCommandApiMixin} 上。</p>
 */
@Mixin(targets = "org.bukkit.entity.minecart.CommandMinecart", remap = false)
public interface CommandMinecartIfaceMixin extends io.papermc.paper.command.CommandBlockHolder {
}
