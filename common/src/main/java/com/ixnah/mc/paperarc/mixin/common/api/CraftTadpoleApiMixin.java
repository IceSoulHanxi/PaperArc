package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.bukkit.craftbukkit.v.entity.CraftTadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Tadpole age-lock API.
 *
 * Paper stores the lock in a new public {@code AgeableMob.ageLocked} field added
 * by its server patches; vanilla 1.21.1 NMS has no such field, so the state is
 * kept in the ApiState side map keyed by the NMS entity (vanilla aging logic is
 * not gated by this flag - only the API view is stored).
 */
@Mixin(CraftTadpole.class)
public abstract class CraftTadpoleApiMixin {

    @Unique
    private static final String PAPERARC$AGE_LOCKED = "paperarc:ageLocked";

    @Shadow
    public abstract Tadpole getHandle();

    @Unique
    public boolean getAgeLock() {
        return com.ixnah.mc.paperarc.bridge.ApiState.get(getHandle(), PAPERARC$AGE_LOCKED, Boolean.FALSE);
    }

    @Unique
    public void setAgeLock(boolean lock) {
        com.ixnah.mc.paperarc.bridge.ApiState.put(getHandle(), PAPERARC$AGE_LOCKED, lock);
    }
}
