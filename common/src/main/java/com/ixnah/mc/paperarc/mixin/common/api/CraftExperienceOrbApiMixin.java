package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
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
 * {@code sourceEntityId}, {@code triggerEntityId} and {@code spawnReason} do
 * not exist in the runtime NMS jar and Arclight adds no tracking either, so those
 * getters read the ApiState side-map and default to {@code null} / {@code UNKNOWN};
 * nothing populates them until a spawn-site tracking bridge lands.
 */
@Mixin(CraftExperienceOrb.class)
public abstract class CraftExperienceOrbApiMixin {

    @Shadow
    public abstract net.minecraft.world.entity.ExperienceOrb getHandle();

    @Unique
    private static final String PAPERARC$KEY_SOURCE = "paperarc$sourceEntityId";
    @Unique
    private static final String PAPERARC$KEY_TRIGGER = "paperarc$triggerEntityId";

    // Owner for side-map entries: the NMS orb, so state survives Craft mirror recreation.
    @Unique
    private Object paperarc$owner() {
        return this.getHandle();
    }

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
        return ApiState.get(paperarc$owner(), PAPERARC$KEY_SOURCE, null);
    }

    @Unique
    public UUID getTriggerEntityId() {
        return ApiState.get(paperarc$owner(), PAPERARC$KEY_TRIGGER, null);
    }

}
