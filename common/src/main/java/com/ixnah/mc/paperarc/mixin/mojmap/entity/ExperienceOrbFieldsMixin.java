package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.ExperienceOrbBridge;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code ExperienceOrb.sourceEntityId}/{@code triggerEntityId}
 * supplementary fields (ExperienceOrbs-API-for-Reason-Source-Triggering-play
 * .patch). Field names match Paper exactly (no {@code paperarc$} prefix) for
 * reflection ABI compatibility; access methods carry the {@code paper$} prefix
 * through {@link ExperienceOrbBridge} because Paper's patch adds no NMS
 * accessor.
 *
 * <p>{@code spawnReason} is not injected: its type is the paper-api enum which
 * is absent from the Arclight runtime (would crash mixin application).</p>
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbFieldsMixin implements ExperienceOrbBridge {

    @Unique
    public java.util.UUID sourceEntityId; // Paper

    @Unique
    public java.util.UUID triggerEntityId; // Paper

    @Override
    public java.util.UUID paper$getSourceEntityId() {
        return this.sourceEntityId;
    }

    @Override
    public void paper$setSourceEntityId(java.util.UUID sourceEntityId) {
        this.sourceEntityId = sourceEntityId;
    }

    @Override
    public java.util.UUID paper$getTriggerEntityId() {
        return this.triggerEntityId;
    }

    @Override
    public void paper$setTriggerEntityId(java.util.UUID triggerEntityId) {
        this.triggerEntityId = triggerEntityId;
    }
}
