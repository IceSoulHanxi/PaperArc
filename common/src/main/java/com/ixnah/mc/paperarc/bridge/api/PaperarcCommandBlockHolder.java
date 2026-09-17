package com.ixnah.mc.paperarc.bridge.api;

import net.minecraft.core.RegistryAccess;

/**
 * {@code io.papermc.paper.command.CommandBlockHolder} 实现体共用的小工具。
 *
 * <p>1.21.1 的 {@code Component.Serializer.toJson/fromJson} 都多了一个
 * {@code RegistryAccess} 参数（{@code docs/mixin-conventions.md} 的 1.21.1 差异表），
 * 命令方块与命令矿车两边都要取，抽到这里免得各写一份。
 */
public final class PaperarcCommandBlockHolder {

    private PaperarcCommandBlockHolder() {
    }

    public static RegistryAccess registryAccess() {
        return ((org.bukkit.craftbukkit.v.CraftServer) com.ixnah.mc.paperarc.bridge.PaperArcBridge.getServer())
                .getServer().registryAccess();
    }

    /**
     * 给"运行时没有 PDC 但 paper 认为有"的宿主（目前只有 {@code Raid}）新建
     * {@code CraftPersistentDataContainer} 用的类型注册表。spigot 的每个宿主类都有自己的
     * 一份 private static，够不着，这里自建一份 —— 它只是适配器缓存，多一份没有副作用。
     */
    public static final org.bukkit.craftbukkit.v.persistence.CraftPersistentDataTypeRegistry PDC_REGISTRY =
            new org.bukkit.craftbukkit.v.persistence.CraftPersistentDataTypeRegistry();
}
