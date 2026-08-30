package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Vex;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftMob;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftVex;
import org.bukkit.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;

/**
 * Adds Paper's Vex summoner and limited-lifetime methods
 * (patches/server/Vex-get-setSummoner-API.patch + Missing-Entity-API.patch -> CraftVex).
 *
 * Summoner access maps to vanilla public {@code Vex#getOwner()/setOwner(Mob)}.
 * The lifetime state lives in vanilla private fields {@code hasLimitedLife}
 * and {@code limitedLifeTicks}, widened via AT (f_33978_ / f_33979_) and
 * accessed directly — no reflection.
 */
@Mixin(CraftVex.class)
public abstract class CraftVexApiMixin {

    @Shadow
    public abstract Vex getHandle();

    @Unique
    public Mob getSummoner() {
        net.minecraft.world.entity.Mob owner = getHandle().getOwner();
        // getBukkitEntity() 是 CraftBukkit 运行时注入方法，编译期不可见，走桥接工厂
        return owner != null ? PaperArcBridge.<Mob>bukkitEntity(owner) : null;
    }

    @Unique
    public void setSummoner(Mob summoner) {
        getHandle().setOwner(summoner == null ? null : ((CraftMob) summoner).getHandle());
    }

    @Unique
    public boolean hasLimitedLifetime() {
        return getHandle().hasLimitedLife;
    }

    @Unique
    public void setLimitedLifetime(boolean hasLimitedLifetime) {
        getHandle().hasLimitedLife = hasLimitedLifetime;
    }

    @Unique
    public int getLimitedLifetimeTicks() {
        return getHandle().limitedLifeTicks;
    }

    @Unique
    public void setLimitedLifetimeTicks(int ticks) {
        getHandle().limitedLifeTicks = ticks;
    }
}
