package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import org.bukkit.craftbukkit.v.entity.CraftExperienceOrb;
import org.bukkit.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Adds Paper's ExperienceOrb API (ExperienceOrbs-API-for-Reason-Source-Triggering-play
 * + ExperienceOrb-merging-stacking-API).
 *
 * {@code count} exists as a private vanilla field (reflection). The CB-added NMS
 * fields {@code sourceEntityId}, {@code triggerEntityId} and {@code spawnReason} do
 * not exist in the runtime NMS jar and Arclight adds no tracking either, so those
 * getters read the ApiState side-map and default to {@code null}; nothing populates
 * them until a spawn-site tracking bridge lands.
 */
@Mixin(CraftExperienceOrb.class)
public abstract class CraftExperienceOrbApiMixin {

    @Shadow
    public abstract net.minecraft.world.entity.ExperienceOrb getHandle();

    @Unique
    private static final String PAPERARC$KEY_SOURCE = "paperarc$sourceEntityId";
    @Unique
    private static final String PAPERARC$KEY_TRIGGER = "paperarc$triggerEntityId";
    @Unique
    private static final String PAPERARC$KEY_REASON = "paperarc$spawnReason";

    // Owner for side-map entries: the NMS orb, so state survives Craft mirror recreation.
    @Unique
    private Object paperarc$owner() {
        return this.getHandle();
    }

    @Unique
    private int paperarc$count() {
        try {
            Field f = net.minecraft.world.entity.ExperienceOrb.class.getDeclaredField("count");
            f.setAccessible(true);
            return f.getInt(getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ExperienceOrb.count not found", e);
        }
    }

    @Unique
    private void paperarc$count(int count) {
        try {
            Field f = net.minecraft.world.entity.ExperienceOrb.class.getDeclaredField("count");
            f.setAccessible(true);
            f.setInt(getHandle(), count);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ExperienceOrb.count not found", e);
        }
    }

    @Unique
    public int getCount() {
        return paperarc$count();
    }

    @Unique
    public void setCount(int count) {
        paperarc$count(count);
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
