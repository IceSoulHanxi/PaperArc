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

    @Unique
    public BossBar getBossBar() {
        return new CraftBossBar(this.handle.raidEvent);
    }
}
