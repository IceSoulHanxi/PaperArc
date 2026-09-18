package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.raid.Raid;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v.CraftRaid;
import org.bukkit.craftbukkit.v.boss.CraftBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


/**
 * Port of Paper's More-Raid-API.patch additions on {@link CraftRaid}
 * ({@code Raid#getBossBar()} and {@code Raid#getId()}).
 */
@Mixin(CraftRaid.class)
public abstract class CraftRaidApiMixin {

    @Shadow
    private Raid handle;

    @Unique
    public int getId() {
        return this.handle.getId();
    }

    /**
     * Paper exposes the raid's boss event directly; the field is private in
     * vanilla mappings and opened by {@code paperarc.accesswidener}.
     */
    @Unique
    public BossBar getBossBar() {
        ServerBossEvent raidEvent = this.handle.raidEvent;
        return new CraftBossBar(raidEvent);
    }

    /**
     * paper 的 {@code Raid extends PersistentDataHolder}（B3-2）。
     *
     * <p>Paper 在 NMS {@code Raid} 上加了一个 {@code persistentDataContainer} 字段并随
     * {@code raids.dat} 存档。B8/Y-3 照做：容器挪到 NMS 字段
     * （{@code entity.RaidPersistentDataMixin}）并真的落盘，不再是重启即丢的
     * {@code ApiState}。宿主必须是 NMS Raid —— CraftRaid 是每次查询新建的包装。
     */
    @Unique
    public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
        return ((com.ixnah.mc.paperarc.bridge.RaidPersistentDataBridge)
                ((org.bukkit.craftbukkit.v.CraftRaid) (Object) this).getHandle())
                .paper$persistentDataContainer();
    }
}
