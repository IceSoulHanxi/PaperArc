package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.raid.Raid;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_20_R1.CraftRaid;
import org.bukkit.craftbukkit.v1_20_R1.boss.CraftBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

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
        return new CraftBossBar(this.paperarc$getRaidEvent());
    }

    /**
     * Paper exposes the raid's boss event directly; the field is private in
     * vanilla mappings and we cannot widen it from a single-target mixin,
     * so read it reflectively (Arclight runs Mojang-mapped at runtime).
     */
    @Unique
    private ServerBossEvent paperarc$getRaidEvent() {
        try {
            Field field = Raid.class.getDeclaredField("raidEvent");
            field.setAccessible(true);
            return (ServerBossEvent) field.get(this.handle);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to access Raid#raidEvent", e);
        }
    }
}
