package io.izzel.arclight.common.bridge.core.world;

import org.bukkit.craftbukkit.v.CraftWorld;

/**
 * Arclight {@code WorldBridge} 的**编译期桩**（只声明我们用到的那一个方法）。
 *
 * <p>运行时 NMS {@code Level} 由 Arclight 的 {@code LevelMixin} 实现这个接口，
 * {@code bridge$getWorld()} 转发到 {@code getWorld()} —— 后者先读 {@code world}
 * 字段，为 null 才新建（`javap -c` 核对），所以每个 {@code ServerLevel} 的
 * {@code CraftWorld} 是稳定的同一个对象。走这个入口即可去掉按字面名反射 getWorld。
 *
 * <p>这个源码集不进产物，见同源码集 {@code EntityClassLookup} 的注释。
 */
public interface WorldBridge {

    CraftWorld bridge$getWorld();

    /**
     * Arclight 在每次 {@code addFreshEntity/addWithUUID/…} 之前把生成原因压进
     * {@code ServerLevel} 上的一个字段，{@code addEntity} 里读出来派发
     * {@code CreatureSpawnEvent} 之后就清掉。A8/Y-3 用它在 {@code addEntity} 的 HEAD
     * 把原因写进 {@code Entity.spawnReason}（Paper 是直接在
     * {@code addFreshEntity(Entity, SpawnReason)} 里写）。
     */
    org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason bridge$getAddEntityReason();
}
