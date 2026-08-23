package dev.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Vex;
import org.bukkit.craftbukkit.v.entity.CraftMob;
import org.bukkit.craftbukkit.v.entity.CraftVex;
import org.bukkit.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

import dev.paperarc.bridge.PaperArcBridge;

/**
 * Adds Paper's Vex summoner and limited-lifetime methods
 * (patches/server/Vex-get-setSummoner-API.patch + Missing-Entity-API.patch -> CraftVex).
 *
 * Summoner access maps to vanilla public {@code Vex#getOwner()/setOwner(Mob)}.
 * The lifetime state lives in vanilla private fields {@code hasLimitedLife}
 * and {@code limitedLifeTicks} (Paper's NMS patch only widens access), so they
 * are read/written reflectively.
 */
@Mixin(CraftVex.class)
public abstract class CraftVexApiMixin {

    @Shadow
    public abstract Vex getHandle();

    @Unique
    private static volatile Field PAPERARC$HAS_LIMITED_LIFE;

    @Unique
    private static volatile Field PAPERARC$LIMITED_LIFE_TICKS;

    @Unique
    private static synchronized void paperarc$resolveFields() {
        if (PAPERARC$HAS_LIMITED_LIFE != null) {
            return;
        }
        try {
            Field hasLimitedLife = Vex.class.getDeclaredField("hasLimitedLife");
            hasLimitedLife.setAccessible(true);
            PAPERARC$HAS_LIMITED_LIFE = hasLimitedLife;
            Field limitedLifeTicks = Vex.class.getDeclaredField("limitedLifeTicks");
            limitedLifeTicks.setAccessible(true);
            PAPERARC$LIMITED_LIFE_TICKS = limitedLifeTicks;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Vex lifetime fields not found", e);
        }
    }

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
        paperarc$resolveFields();
        try {
            return PAPERARC$HAS_LIMITED_LIFE.getBoolean(getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Vex.hasLimitedLife not accessible", e);
        }
    }

    @Unique
    public void setLimitedLifetime(boolean hasLimitedLifetime) {
        paperarc$resolveFields();
        try {
            PAPERARC$HAS_LIMITED_LIFE.setBoolean(getHandle(), hasLimitedLifetime);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to write NMS Vex.hasLimitedLife", e);
        }
    }

    @Unique
    public int getLimitedLifetimeTicks() {
        paperarc$resolveFields();
        try {
            return PAPERARC$LIMITED_LIFE_TICKS.getInt(getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Vex.limitedLifeTicks not accessible", e);
        }
    }

    @Unique
    public void setLimitedLifetimeTicks(int ticks) {
        paperarc$resolveFields();
        try {
            PAPERARC$LIMITED_LIFE_TICKS.setInt(getHandle(), ticks);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to write NMS Vex.limitedLifeTicks", e);
        }
    }
}
