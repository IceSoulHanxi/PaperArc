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
}
