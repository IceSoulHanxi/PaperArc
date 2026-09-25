package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcOfflinePlayerPdc;
import org.bukkit.craftbukkit.v.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.File;

/**
 * {@code OfflinePlayer#getPersistentDataContainer()} 的离线侧实现体（gaps.md §3.1 main 独有行）。
 *
 * <p>在线时直接转给在线实体的容器（同 Paper）；离线时从
 * {@code <world>/playerdata/<uuid>.dat} 根上的 {@code BukkitValues} 读出来，
 * 写入立刻回写同一个文件（见 {@link PaperarcOfflinePlayerPdc}）。
 * B7/Y-4 之前这里是"每次新建空容器"的降级实现，返回类型还写成了
 * {@code org.bukkit.persistence.PersistentDataContainer} —— paper-api 上
 * {@code OfflinePlayer} 继承的是 {@code PersistentDataViewHolder}，声明的返回类型是
 * {@code PersistentDataContainerView}，描述符对不上等于插件调用时 AbstractMethodError。
 */
@Mixin(CraftOfflinePlayer.class)
public abstract class CraftOfflinePlayerPdcApiMixin {

    @Unique
    private static final CraftPersistentDataTypeRegistry PAPERARC$PDC_REGISTRY =
            new CraftPersistentDataTypeRegistry();

    @Shadow(remap = false)
    private File getDataFile() {
        throw new AssertionError();
    }

    @Unique
    public io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer() {
        CraftOfflinePlayer self = (CraftOfflinePlayer) (Object) this;
        org.bukkit.entity.Player online = self.getPlayer();
        if (online != null) {
            return online.getPersistentDataContainer();
        }
        return new PaperarcOfflinePlayerPdc(this.getDataFile(), PAPERARC$PDC_REGISTRY);
    }
}
