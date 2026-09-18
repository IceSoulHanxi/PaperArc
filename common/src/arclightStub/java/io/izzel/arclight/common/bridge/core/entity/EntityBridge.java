package io.izzel.arclight.common.bridge.core.entity;

import org.bukkit.craftbukkit.v.entity.CraftEntity;

/**
 * Arclight {@code EntityBridge} 的**编译期桩**（只声明我们用到的那一个方法）。
 *
 * <p>运行时 NMS {@code Entity} 由 Arclight 的 {@code EntityMixin} 实现这个接口，
 * {@code bridge$getBukkitEntity()} 转发到 {@code internal$getBukkitEntity()} ——
 * 后者先读 {@code bukkitEntity} 字段，为 null 才调
 * {@code CraftEntity.getEntity(server, nms)} 并回填（`javap -c` 核对）。
 * 因此**必须**走这个入口取包装对象：直接调静态工厂每次都会 new 一个新的 Craft 实体，
 * Craft 侧 {@code @Unique} 状态字段随之丢失（checklist §1.10 an）。
 *
 * <p>这个源码集不进产物，见同源码集 {@code EntityClassLookup} 的注释。
 */
public interface EntityBridge {

    CraftEntity bridge$getBukkitEntity();
}
