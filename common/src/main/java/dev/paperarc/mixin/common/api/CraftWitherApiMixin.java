package dev.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.entity.CraftWither;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.paperarc.bridge.ApiState;
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
        // Paper: return getHandle().canUsePortal(false);
        return this.getHandle().canUsePortal(false);
    }

    @Unique
    public void setCanTravelThroughPortals(boolean value) {
        // Paper sets a private WitherBoss#canPortal field that vanilla mojmap lacks -> side-map.
        ApiState.put(this.getHandle(), "canTravelThroughPortals", value);
    }

    @Unique
    public void enterInvulnerabilityPhase() {
        // Paper: this.getHandle().makeInvulnerable();
        this.getHandle().makeInvulnerable();
    }
}
