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
 * {@code sourceEntityId} and {@code triggerEntityId} are injected into the NMS
 * orb by {@code ExperienceOrbFieldsMixin} and reached through
 * {@link com.ixnah.mc.paperarc.bridge.ExperienceOrbBridge}; they default to
 * {@code null} until a spawn-site tracking bridge populates them.
 *
 * <p>{@code spawnReason} is not exposed (its paper-api enum type is absent from
 * the Arclight runtime).</p>
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

}
