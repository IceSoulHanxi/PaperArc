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
     * {@code raids.dat} 存档；我们用 {@code ApiState} 按 NMS Raid 对象挂一份等价容器，
     * <b>不做持久化</b>（服务器重启后清空，已记 docs/gaps.md）。
     * 键用 NMS Raid 而不是 CraftRaid：CraftRaid 是每次查询新建的包装。
     */
    @Unique
    public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
        net.minecraft.world.entity.raid.Raid handle = ((org.bukkit.craftbukkit.v.CraftRaid) (Object) this).getHandle();
        org.bukkit.persistence.PersistentDataContainer existing =
                com.ixnah.mc.paperarc.bridge.ApiState.get(handle, "paperarc:pdc", null);
        if (existing != null) {
            return existing;
        }
        org.bukkit.persistence.PersistentDataContainer created =
                new org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer(
                        com.ixnah.mc.paperarc.bridge.api.PaperarcCommandBlockHolder.PDC_REGISTRY);
        com.ixnah.mc.paperarc.bridge.ApiState.put(handle, "paperarc:pdc", created);
        return created;
    }
}
