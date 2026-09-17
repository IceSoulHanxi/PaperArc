package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code OfflinePlayer#getPersistentDataContainer()} 的离线侧实现体。
 *
 * <p>**降级实现**：Paper 把离线玩家的 PDC 存进玩家存档文件（读写都要碰
 * {@code <world>/playerdata/<uuid>.dat} 的 {@code BukkitValues}），Arclight 的
 * {@code CraftOfflinePlayer} 既不缓存也不提供写回入口。这里返回一个**每次调用新建的
 * 空容器**：够让插件不崩（此前是 {@code AbstractMethodError}），但读不到已有数据、
 * 写入也不会持久化。已登记 docs/gaps.md。</p>
 */
@Mixin(CraftOfflinePlayer.class)
public abstract class CraftOfflinePlayerPdcApiMixin {

    @Unique
    private static final CraftPersistentDataTypeRegistry PAPERARC$PDC_REGISTRY =
            new CraftPersistentDataTypeRegistry();

    @Unique
    public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
        return new CraftPersistentDataContainer(PAPERARC$PDC_REGISTRY);
    }
}
