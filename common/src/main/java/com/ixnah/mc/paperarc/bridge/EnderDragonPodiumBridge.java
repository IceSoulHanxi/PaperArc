package com.ixnah.mc.paperarc.bridge;

import net.minecraft.core.BlockPos;

/**
 * Paper 给 NMS {@code EnderDragon} 加的 {@code podium} 字段（Ender-Dragon-Events / Podium API）。
 *
 * <p>A8/Y-3 从 Craft 侧挪到 NMS 侧：{@code CraftEnderDragon} 虽然由
 * {@code bukkitEntity} 缓存、身份稳定，但 Paper 的真相在 NMS 实体上，
 * 放这里两分支与 Paper 的语义一致（重新包装、跨插件取用都拿得到同一份）。
 * 与 Paper 一样**不落盘**（重启后回到默认祭坛）。
 */
public interface EnderDragonPodiumBridge {

    BlockPos paper$getPodium();

    void paper$setPodium(BlockPos podium);
}
