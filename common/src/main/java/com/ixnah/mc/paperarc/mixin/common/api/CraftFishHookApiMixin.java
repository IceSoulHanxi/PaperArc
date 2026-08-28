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
 * {@code fishAngle}, {@code lureSpeed}) consists of private vanilla fields with no
 * accessors, so all access goes through cached reflective lookups (mojmap runtime
 * names).</p>
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

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$TIME_UNTIL_LURED;

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$TIME_UNTIL_HOOKED;

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$LURE_SPEED;

    @Unique
    private static java.lang.reflect.Field paperarc$field(Class<?> owner, String name, Class<?> holder) {
        try {
            java.lang.reflect.Field resolved = owner.getDeclaredField(name);
            resolved.setAccessible(true);
            return resolved;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS " + owner.getSimpleName() + "." + name + " field not found", e);
        }
    }

    @Unique
    private static java.lang.reflect.Field paperarc$timeUntilLured() {
        java.lang.reflect.Field f = PAPERARC$TIME_UNTIL_LURED;
        if (f == null) {
            synchronized (CraftFishHookApiMixin.class) {
                if (PAPERARC$TIME_UNTIL_LURED == null) {
                    PAPERARC$TIME_UNTIL_LURED = paperarc$field(FishingHook.class, "timeUntilLured", CraftFishHookApiMixin.class);
                }
                f = PAPERARC$TIME_UNTIL_LURED;
            }
        }
        return f;
    }

    @Unique
    private static java.lang.reflect.Field paperarc$timeUntilHooked() {
        java.lang.reflect.Field f = PAPERARC$TIME_UNTIL_HOOKED;
        if (f == null) {
            synchronized (CraftFishHookApiMixin.class) {
                if (PAPERARC$TIME_UNTIL_HOOKED == null) {
                    PAPERARC$TIME_UNTIL_HOOKED = paperarc$field(FishingHook.class, "timeUntilHooked", CraftFishHookApiMixin.class);
                }
                f = PAPERARC$TIME_UNTIL_HOOKED;
            }
        }
        return f;
    }

    @Unique
    private static java.lang.reflect.Field paperarc$lureSpeed() {
        java.lang.reflect.Field f = PAPERARC$LURE_SPEED;
        if (f == null) {
            synchronized (CraftFishHookApiMixin.class) {
                if (PAPERARC$LURE_SPEED == null) {
                    PAPERARC$LURE_SPEED = paperarc$field(FishingHook.class, "lureSpeed", CraftFishHookApiMixin.class);
                }
                f = PAPERARC$LURE_SPEED;
            }
        }
        return f;
    }

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$FISH_ANGLE;

    // Entity.random is protected in vanilla mojmap; reached reflectively.
    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$RANDOM;

    @Unique
    private static net.minecraft.util.RandomSource paperarc$random(FishingHook hook) {
        java.lang.reflect.Field f = PAPERARC$RANDOM;
        if (f == null) {
            synchronized (CraftFishHookApiMixin.class) {
                if (PAPERARC$RANDOM == null) {
                    try {
                        java.lang.reflect.Field resolved = net.minecraft.world.entity.Entity.class.getDeclaredField("random");
                        resolved.setAccessible(true);
                        PAPERARC$RANDOM = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Entity.random field not found", e);
                    }
                }
                f = PAPERARC$RANDOM;
            }
        }
        try {
            return (net.minecraft.util.RandomSource) f.get(hook);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS Entity.random", e);
        }
    }

    @Unique
    private static java.lang.reflect.Field paperarc$fishAngle() {
        java.lang.reflect.Field f = PAPERARC$FISH_ANGLE;
        if (f == null) {
            synchronized (CraftFishHookApiMixin.class) {
                if (PAPERARC$FISH_ANGLE == null) {
                    PAPERARC$FISH_ANGLE = paperarc$field(FishingHook.class, "fishAngle", CraftFishHookApiMixin.class);
                }
                f = PAPERARC$FISH_ANGLE;
            }
        }
        return f;
    }

    @Unique
    private int paperarc$getInt(java.lang.reflect.Field field, FishingHook hook) {
        try {
            return field.getInt(hook);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS FishingHook." + field.getName(), e);
        }
    }

    @Unique
    private void paperarc$setInt(java.lang.reflect.Field field, FishingHook hook, int value) {
        try {
            field.setInt(hook, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS FishingHook." + field.getName(), e);
        }
    }

    /**
     * Paper-extracted {@code FishingHook#resetTimeUntilLured()} re-inlined against the
     * vanilla defaults ({@code minWaitTime=100}, {@code maxWaitTime=600}) because the
     * CraftBukkit tuning fields are absent in this runtime.
     */
    @Unique
    private void paperarc$resetTimeUntilLured(FishingHook hook) {
        int timeUntilLured = Mth.nextInt(paperarc$random(hook), 100, 600);
        int lureSpeed = paperarc$getInt(paperarc$lureSpeed(), hook);
        timeUntilLured -= (lureSpeed >= 600) ? (timeUntilLured - 1) : lureSpeed; // Paper - Fix Lure infinite loop
        paperarc$setInt(paperarc$timeUntilLured(), hook, timeUntilLured);
    }

    @Unique
    public int getWaitTime() {
        return paperarc$getInt(paperarc$timeUntilLured(), getHandle());
    }

    @Unique
    public void setWaitTime(int ticks) {
        paperarc$setInt(paperarc$timeUntilLured(), getHandle(), ticks);
    }

    @Unique
    public int getTimeUntilBite() {
        return paperarc$getInt(paperarc$timeUntilHooked(), getHandle());
    }

    @Unique
    public void setTimeUntilBite(final int ticks) {
        Preconditions.checkArgument(ticks >= 1, "Cannot set time until bite to less than 1 (%s<1)", ticks);
        final FishingHook hook = getHandle();

        // Reset the fish angle only when this call "enters" the fish into the lure stage.
        final boolean alreadyInLuringPhase =
                paperarc$getInt(paperarc$timeUntilHooked(), hook) > 0
                        && paperarc$getInt(paperarc$timeUntilLured(), hook) <= 0;
        if (!alreadyInLuringPhase) {
            try {
                paperarc$fishAngle().setFloat(hook, Mth.nextFloat(paperarc$random(hook), 0.0F, 360.0F));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to write NMS FishingHook.fishAngle", e);
            }
            paperarc$setInt(paperarc$timeUntilLured(), hook, 0);
        }

        paperarc$setInt(paperarc$timeUntilHooked(), hook, ticks);
    }

    @Unique
    public void resetFishingState() {
        final FishingHook hook = getHandle();
        paperarc$resetTimeUntilLured(hook);
        // Reset time until hooked, will be repopulated once lured time is ticked down.
        paperarc$setInt(paperarc$timeUntilHooked(), hook, 0);
    }
}
