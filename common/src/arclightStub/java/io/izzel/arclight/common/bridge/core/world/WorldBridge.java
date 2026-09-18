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
}
