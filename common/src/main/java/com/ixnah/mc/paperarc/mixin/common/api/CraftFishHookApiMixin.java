package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.ApiState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftFishHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's More-Projectile-API (FishHook part) to {@link CraftFishHook}.
 *
 * <p>The referenced NMS state ({@code timeUntilLured}, {@code timeUntilHooked},
 * {@code fishAngle}, {@code lureSpeed}) consists of private vanilla fields with
 * no accessors, widened via AT (f_37090_ / f_37091_ / f_37092_ / f_37097_);
 * {@code Entity.random} is also AT-widened (f_19796_) — all accessed directly,
 * no reflection.</p>
 *
 * <p>Paper's implementation relies on CraftBukkit-added tuning fields
 * ({@code minWaitTime}/{@code maxWaitTime}/{@code applyLure}/{@code minLureAngle}/
 * {@code maxLureAngle}) which do not exist in this vanilla-NMS runtime; the vanilla
 * defaults (wait 100..600 ticks, lure speed applied, angle 0..360) are used instead.</p>
 */
@Mixin(CraftFishHook.class)
public abstract class CraftFishHookApiMixin {

    @Shadow
    public abstract FishingHook getHandle();

    /**
     * Paper-extracted {@code FishingHook#resetTimeUntilLured()} re-inlined against the
     * vanilla defaults ({@code minWaitTime=100}, {@code maxWaitTime=600}) because the
     * CraftBukkit tuning fields are absent in this runtime.
     */
    @Unique
    private void paperarc$resetTimeUntilLured(FishingHook hook) {
        int timeUntilLured = Mth.nextInt(hook.random, 100, 600);
        int lureSpeed = hook.lureSpeed;
        timeUntilLured -= (lureSpeed >= 600) ? (timeUntilLured - 1) : lureSpeed; // Paper - Fix Lure infinite loop
        hook.timeUntilLured = timeUntilLured;
    }

    @Unique
    public int getWaitTime() {
        return getHandle().timeUntilLured;
    }

    @Unique
    public void setWaitTime(int ticks) {
        getHandle().timeUntilLured = ticks;
    }

    @Unique
    public int getTimeUntilBite() {
        return getHandle().timeUntilHooked;
    }

    @Unique
    public void setTimeUntilBite(final int ticks) {
        Preconditions.checkArgument(ticks >= 1, "Cannot set time until bite to less than 1 (%s<1)", ticks);
        final FishingHook hook = getHandle();

        // Reset the fish angle only when this call "enters" the fish into the lure stage.
        final boolean alreadyInLuringPhase =
                hook.timeUntilHooked > 0 && hook.timeUntilLured <= 0;
        if (!alreadyInLuringPhase) {
            hook.fishAngle = Mth.nextFloat(hook.random, 0.0F, 360.0F);
            hook.timeUntilLured = 0;
        }

        hook.timeUntilHooked = ticks;
    }

    @Unique
    public void resetFishingState() {
        final FishingHook hook = getHandle();
        paperarc$resetTimeUntilLured(hook);
        // Reset time until hooked, will be repopulated once lured time is ticked down.
        hook.timeUntilHooked = 0;
    }
}
