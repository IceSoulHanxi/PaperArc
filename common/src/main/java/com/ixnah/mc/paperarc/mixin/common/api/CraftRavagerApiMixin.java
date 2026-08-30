package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Ravager;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftRavager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Missing-Entity-API (Missing Entity Behavior) additions on
 * {@link CraftRavager}.
 *
 * Getters delegate to public NMS accessors; the tick fields
 * ({@code attackTick}/{@code stunnedTick}/{@code roarTick}) are private in
 * vanilla NMS — widened via AT (f_33320_ / f_33321_ / f_33322_) and written
 * directly, no reflection.
 */
@Mixin(CraftRavager.class)
public abstract class CraftRavagerApiMixin {

    @Shadow
    public abstract Ravager getHandle();

    // Paper start - Missing Entity Behavior
    @Unique
    public int getAttackTicks() {
        return this.getHandle().getAttackTick();
    }

    @Unique
    public void setAttackTicks(int ticks) {
        this.getHandle().attackTick = ticks;
    }

    @Unique
    public int getStunnedTicks() {
        return this.getHandle().getStunnedTick();
    }

    @Unique
    public void setStunnedTicks(int ticks) {
        this.getHandle().stunnedTick = ticks;
    }

    @Unique
    public int getRoarTicks() {
        return this.getHandle().getRoarTick();
    }

    @Unique
    public void setRoarTicks(int ticks) {
        this.getHandle().roarTick = ticks;
    }
    // Paper end - Missing Entity Behavior
}
