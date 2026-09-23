package org.bukkit.registry;

import org.bukkit.NamespacedKey;

/**
 * 编译期桩：Spigot 1.21.11 引入的接口，供 Arclight 生成的 CraftBukkit (如 CraftSound) 引用。
 * 运行时由 Arclight 自带的 Bukkit 提供。
 */
public interface RegistryAware {
    NamespacedKey getKeyOrThrow();
    NamespacedKey getKeyOrNull();
    boolean isRegistered();
}
