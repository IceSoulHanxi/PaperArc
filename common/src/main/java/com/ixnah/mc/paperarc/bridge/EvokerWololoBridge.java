package com.ixnah.mc.paperarc.bridge;

import net.minecraft.world.entity.animal.Sheep;

/**
 * Paper 给 NMS {@code Evoker} 加的 {@code wololoTarget} 字段。
 *
 * <p>A8/Y-3 从 Craft 侧挪到 NMS 侧，理由同 {@link EnderDragonPodiumBridge}。
 * 与 Paper 一样不落盘。
 */
public interface EvokerWololoBridge {

    Sheep paper$getWololoTarget();

    void paper$setWololoTarget(Sheep sheep);
}
