package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v1_20_R1.entity.CraftWither;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.ApiState;
import net.minecraft.world.entity.boss.wither.WitherBoss;

/**
 * Adds Paper's Missing-Entity-API additions to CraftWither.
 *
 * Paper ref: patches/server/Missing-Entity-API.patch (CraftWither + WitherBoss hunks).
 * All methods delegate straight to NMS WitherBoss except setCanTravelThroughPortals:
 * Paper stores the flag in a private {@code canPortal} field added to WitherBoss, which
 * does not exist in vanilla mojmap NMS, so it is kept in the ApiState side-map keyed by
 * the handle instance (no vanilla consumer reads it; noted in report).
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
        // Paper stores a private WitherBoss#canPortal field (Missing-Entity-API.patch);
        // vanilla mojmap has no such field, so keep it in the ApiState side-map and
        // honour it at read time.
        ApiState.put(this.getHandle(), "canPortal", value);
    }

    @Unique
    public void enterInvulnerabilityPhase() {
        // Paper: this.getHandle().makeInvulnerable();
        this.getHandle().makeInvulnerable();
    }

    @Unique
    public boolean canChangeDimensions() {
        Boolean portal = ApiState.get(this.getHandle(), "canPortal", null);
        if (portal != null && !portal) {
            return false;
        }
        return this.getHandle().canChangeDimensions();
    }
}
