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
        // Paper 1.20.1: return getHandle().canChangeDimensions(); (canUsePortal is 1.21+)
        return this.getHandle().canChangeDimensions();
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

    @Unique
    public boolean canChangeDimensions() {
        if (!((WitherBossBridge) this.getHandle()).paper$canPortal()) {
            return false;
        }
        return this.getHandle().canChangeDimensions();
    }
}
