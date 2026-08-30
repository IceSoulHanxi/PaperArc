package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ExperienceOrbBridge;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Adds Paper's ExperienceOrb API (ExperienceOrbs-API-for-Reason-Source-Triggering-play
 * + ExperienceOrb-merging-stacking-API).
 *
 * {@code count} is widened via AT (public net.minecraft.world.entity.ExperienceOrb
 * f_147072_) and accessed directly — no reflection. The CB-added NMS fields
 * {@code sourceEntityId}, {@code triggerEntityId} and {@code spawnReason} are
 * injected into the NMS orb by {@code ExperienceOrbFieldsMixin} and reached
 * through {@link com.ixnah.mc.paperarc.bridge.ExperienceOrbBridge}; they
 * default to {@code null} (IDs) / {@code UNKNOWN} (spawnReason) until a
 * spawn-site tracking bridge populates them.
 *
 * <p>{@code spawnReason} is exposed again: its paper-api enum type
 * {@code org.bukkit.entity.ExperienceOrb.SpawnReason} is injected into the
 * classloader at mod construction by {@code RuntimeClassInjector} (embedded
 * class bytes), so the runtime now has the type needed by the mixin signature.</p>
 */
@Mixin(CraftExperienceOrb.class)
public abstract class CraftExperienceOrbApiMixin {

    @Shadow
    public abstract net.minecraft.world.entity.ExperienceOrb getHandle();

    @Unique
    public int getCount() {
        return this.getHandle().count;
    }

    @Unique
    public void setCount(int count) {
        this.getHandle().count = count;
    }

    @Unique
    public UUID getSourceEntityId() {
        return ((ExperienceOrbBridge) this.getHandle()).paper$getSourceEntityId();
    }

    @Unique
    public UUID getTriggerEntityId() {
        return ((ExperienceOrbBridge) this.getHandle()).paper$getTriggerEntityId();
    }

    @Unique
    public org.bukkit.entity.ExperienceOrb.SpawnReason getSpawnReason() {
        int ordinal = ((ExperienceOrbBridge) this.getHandle()).paper$getSpawnReasonOrdinal();
        org.bukkit.entity.ExperienceOrb.SpawnReason[] values =
                org.bukkit.entity.ExperienceOrb.SpawnReason.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal]
                : org.bukkit.entity.ExperienceOrb.SpawnReason.UNKNOWN;
    }

}
