package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.WitherBossBridge;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.bukkit.craftbukkit.v.entity.CraftWither;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Missing-Entity-API additions to CraftWither.
 *
 * <p>Paper ref: patches/server/Missing-Entity-API.patch (CraftWither + WitherBoss hunks).
 * All methods delegate straight to NMS WitherBoss except setCanTravelThroughPortals:
 * Paper stores the flag in a private {@code canPortal} field added to WitherBoss,
 * injected here by {@code WitherBossFieldsMixin} and reached through
 * {@link WitherBossBridge}. Paper adds a NMS setter
 * {@code setCanTravelThroughPortals(boolean)} (used by the bridge under that name)
 * and no getter, so the read path uses {@code paper$canPortal()}.
 */
@Mixin(CraftWither.class)
public abstract class CraftWitherApiMixin {

    @Shadow
    public abstract WitherBoss getHandle();

    @Unique
    public boolean isCharged() {
        return this.getHandle().isPowered();
    }

    @Unique
    public int getInvulnerableTicks() {
        return this.getHandle().getInvulnerableTicks();
    }

    @Unique
    public void setInvulnerableTicks(int ticks) {
        this.getHandle().setInvulnerableTicks(ticks);
    }

    @Unique
    public boolean canTravelThroughPortals() {
        // 1.21.1：无参的 canChangeDimensions() 已换成 canUsePortal(boolean)
        //（无参版在 1.20.1 存在；1.21.1 只剩 canChangeDimensions(Level, Level)）。
        // allowPassengers=false 对应 Paper 这里的语义：只问实体自身能不能走传送门。
        return this.getHandle().canUsePortal(false);
    }

    @Unique
    public void setCanTravelThroughPortals(boolean value) {
        ((WitherBossBridge) this.getHandle()).setCanTravelThroughPortals(value);
    }

    @Unique
    public void enterInvulnerabilityPhase() {
        // Paper: this.getHandle().makeInvulnerable();
        this.getHandle().makeInvulnerable();
    }

    /** Paper 在 WitherBoss 上覆写 canChangeDimensions 以尊重 API 开关；1.21.1 改成两参签名。 */
    @Unique
    public boolean canChangeDimensions(net.minecraft.world.level.Level from,
                                       net.minecraft.world.level.Level to) {
        if (!((WitherBossBridge) this.getHandle()).paper$canPortal()) {
            return false;
        }
        return this.getHandle().canChangeDimensions(from, to);
    }
}
