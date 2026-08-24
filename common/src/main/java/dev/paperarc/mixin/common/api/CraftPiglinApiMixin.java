package dev.paperarc.mixin.common.api;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.bukkit.craftbukkit.v.entity.CraftPiglin;
import dev.paperarc.bridge.craft.CraftEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Missing-Entity-API piglin methods to {@link CraftPiglin}.
 *
 * <p>{@code Piglin.isChargingCrossbow()} is private in vanilla 1.21.1, so the
 * getter invokes it reflectively; the setter uses the public
 * {@code Piglin#setChargingCrossbow(boolean)}. The dancing setters follow
 * Paper's implementation by manipulating brain memories.</p>
 */
@Mixin(CraftPiglin.clas    @Shadow
    public abstract Piglin getHandle();

    @Unique
    private static volatile java.lang.reflect.Method PAPERARC$IS_CHARGING_CROSSBOW;

    @Unique
    private static java.lang.reflect.Method paperarc$isChargingCrossbow() {
        java.lang.reflect.Method m = PAPERARC$IS_CHARGING_CROSSBOW;
        if (m == null) {
            synchronized (CraftPiglinApiMixin.class) {
                if (PAPERARC$IS_CHARGING_CROSSBOW == null) {
                    try {
                        java.lang.reflect.Method resolved = Piglin.class.getDeclaredMethod("isChargingCrossbow");
                        resolved.setAccessible(true);
                        PAPERARC$IS_CHARGING_CROSSBOW = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Piglin.isChargingCrossbow() not found", e);
                    }
                    m = PAPERARC$IS_CHARGING_CROSSBOW;
                }
            }
        }
        return m;
    }

    @Unique
    public boolean isChargingCrossbow() {
        try {
            return (Boolean) paperarc$isChargingCrossbow().invoke(getHandle());
        } catch (java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to invoke NMS Piglin.isChargingCrossbow()", e);
        }
    }

    @Unique
    public void setChargingCrossbow(boolean chargingCrossbow) {
        getHandle().setChargingCrossbow(chargingCrossbow);
    }

    @Unique
    public boolean isDancing() {
        return getHandle().isDancing();
    }

    @Unique
    public void setDancing(boolean dancing) {
        if (dancing) {
            getHandle().getBrain().setMemory(MemoryModuleType.DANCING, true);
            getHandle().getBrain().setMemory(MemoryModuleType.CELEBRATE_LOCATION, getHandle().getOnPos());
        } else {
            getHandle().getBrain().eraseMemory(MemoryModuleType.DANCING);
            getHandle().getBrain().eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
        }
    }

    @Unique
    public void setDancing(long duration) {
        getHandle().getBrain().setMemoryWithExpiry(MemoryModuleType.DANCING, true, duration);
        getHandle().getBrain().setMemoryWithExpiry(MemoryModuleType.CELEBRATE_LOCATION, getHandle().getOnPos(), duration);
    }
}
