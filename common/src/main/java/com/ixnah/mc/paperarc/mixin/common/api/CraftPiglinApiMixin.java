package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.bukkit.craftbukkit.v.entity.CraftPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Missing-Entity-API piglin methods to {@link CraftPiglin}.
 *
 * <p>{@code Piglin.isChargingCrossbow()} is private in vanilla 1.20.1; it is
 * widened via AT (m_34773_()Z) and called directly; the setter uses the public
 * {@code Piglin#setChargingCrossbow(boolean)}. The dancing setters follow
 * Paper's implementation by manipulating brain memories.</p>
 */
@Mixin(CraftPiglin.class)
public abstract class CraftPiglinApiMixin {

    @Shadow
    public abstract Piglin getHandle();

    @Unique
    public boolean isChargingCrossbow() {
        return getHandle().isChargingCrossbow();
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
