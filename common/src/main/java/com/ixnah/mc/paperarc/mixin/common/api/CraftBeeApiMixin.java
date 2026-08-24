package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.animal.Bee;
import org.bukkit.craftbukkit.v.entity.CraftBee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Bee rolling/pollination API missing from Arclight's CraftBee.
 *
 * Paper stores {@code rollingOverride} as a TriState field injected into the NMS
 * Bee class; that field does not exist in the runtime NMS, so it is kept in the
 * ApiState side map keyed by the handle. {@code isRolling()}/{@code setRolling()}
 * are private in vanilla (Paper widens them via access transformer) and the two
 * pollination counters are package-private/private fields, so all of those are
 * reached reflectively here.
 */
@Mixin(CraftBee.class)
public abstract class CraftBeeApiMixin {

    @Unique
    private static final String PAPERARC$ROLLING_OVERRIDE_KEY = "paperarc$rollingOverride";

    @Shadow
    public abstract Bee getHandle();

    @Unique
    private static volatile Method PAPERARC$IS_ROLLING;
    @Unique
    private static volatile Method PAPERARC$SET_ROLLING;
    @Unique
    private static volatile Field PAPERARC$NUM_CROPS_FIELD;
    @Unique
    private static volatile Field PAPERARC$TICKS_SINCE_POLLINATION_FIELD;

    @Unique
    private static Method paperarc$isRollingMethod() {
        Method m = PAPERARC$IS_ROLLING;
        if (m == null) {
            synchronized (CraftBeeApiMixin.class) {
                if (PAPERARC$IS_ROLLING == null) {
                    try {
                        Method resolved = Bee.class.getDeclaredMethod("isRolling");
                        resolved.setAccessible(true);
                        PAPERARC$IS_ROLLING = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Bee.isRolling() not found", e);
                    }
                }
                m = PAPERARC$IS_ROLLING;
            }
        }
        return m;
    }

    @Unique
    private static Method paperarc$setRollingMethod() {
        Method m = PAPERARC$SET_ROLLING;
        if (m == null) {
            synchronized (CraftBeeApiMixin.class) {
                if (PAPERARC$SET_ROLLING == null) {
                    try {
                        Method resolved = Bee.class.getDeclaredMethod("setRolling", boolean.class);
                        resolved.setAccessible(true);
                        PAPERARC$SET_ROLLING = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Bee.setRolling(boolean) not found", e);
                    }
                }
                m = PAPERARC$SET_ROLLING;
            }
        }
        return m;
    }

    @Unique
    private static Field paperarc$numCropsField() {
        Field f = PAPERARC$NUM_CROPS_FIELD;
        if (f == null) {
            synchronized (CraftBeeApiMixin.class) {
                if (PAPERARC$NUM_CROPS_FIELD == null) {
                    try {
                        Field resolved = Bee.class.getDeclaredField("numCropsGrownSincePollination");
                        resolved.setAccessible(true);
                        PAPERARC$NUM_CROPS_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(
                                "NMS Bee.numCropsGrownSincePollination field not found", e);
                    }
                }
                f = PAPERARC$NUM_CROPS_FIELD;
            }
        }
        return f;
    }

    @Unique
    private static Field paperarc$ticksSincePollinationField() {
        Field f = PAPERARC$TICKS_SINCE_POLLINATION_FIELD;
        if (f == null) {
            synchronized (CraftBeeApiMixin.class) {
                if (PAPERARC$TICKS_SINCE_POLLINATION_FIELD == null) {
                    try {
                        Field resolved = Bee.class.getDeclaredField("ticksWithoutNectarSinceExitingHive");
                        resolved.setAccessible(true);
                        PAPERARC$TICKS_SINCE_POLLINATION_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(
                                "NMS Bee.ticksWithoutNectarSinceExitingHive field not found", e);
                    }
                }
                f = PAPERARC$TICKS_SINCE_POLLINATION_FIELD;
            }
        }
        return f;
    }

    @Unique
    private static boolean paperarc$rawIsRolling(Bee handle) {
        try {
            return (Boolean) paperarc$isRollingMethod().invoke(handle);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read NMS Bee rolling flag", e);
        }
    }

    @Unique
    private TriState paperarc$getRollingOverride(Bee handle) {
        TriState stored = ApiState.get(handle, PAPERARC$ROLLING_OVERRIDE_KEY, null);
        return stored == null ? TriState.NOT_SET : stored;
    }

    @Unique
    public int getCropsGrownSincePollination() {
        try {
            return paperarc$numCropsField().getInt(getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS Bee crop counter", e);
        }
    }

    @Unique
    public void setCropsGrownSincePollination(int crops) {
        try {
            paperarc$numCropsField().setInt(getHandle(), crops);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS Bee crop counter", e);
        }
    }

    @Unique
    public int getTicksSincePollination() {
        try {
            return paperarc$ticksSincePollinationField().getInt(getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS Bee pollination timer", e);
        }
    }

    @Unique
    public void setTicksSincePollination(int ticks) {
        try {
            paperarc$ticksSincePollinationField().setInt(getHandle(), ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS Bee pollination timer", e);
        }
    }

    @Unique
    public TriState getRollingOverride() {
        return paperarc$getRollingOverride(getHandle());
    }

    @Unique
    public void setRollingOverride(TriState rolling) {
        Bee handle = getHandle();
        TriState override = rolling == null ? TriState.NOT_SET : rolling;
        ApiState.put(handle, PAPERARC$ROLLING_OVERRIDE_KEY, override);
        // Mirror Paper's patched NMS setRolling: the roll synched-data flag becomes
        // the override value unless the override is NOT_SET (keep the current roll).
        try {
            boolean effective = override.toBooleanOrElse(paperarc$rawIsRolling(handle));
            paperarc$setRollingMethod().invoke(handle, effective);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to refresh NMS Bee rolling state", e);
        }
    }

    @Unique
    public boolean isRolling() {
        return paperarc$getRollingOverride(getHandle()).toBooleanOrElse(paperarc$rawIsRolling(getHandle()));
    }
}
