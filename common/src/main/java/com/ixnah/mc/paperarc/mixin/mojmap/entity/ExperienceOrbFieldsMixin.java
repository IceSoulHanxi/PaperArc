package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.ExperienceOrbBridge;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code ExperienceOrb.sourceEntityId}/{@code triggerEntityId}
 * supplementary fields and the {@code spawnReason} ordinal
 * (ExperienceOrbs-API-for-Reason-Source-Triggering-play.patch). Field names
 * match Paper exactly (no {@code paperarc$} prefix) for reflection ABI
 * compatibility; access methods carry the {@code paper$} prefix through
 * {@link ExperienceOrbBridge} because Paper's patch adds no NMS accessor.
 *
 * <p>{@code spawnReason} is stored as an {@code int} ordinal, NOT the paper-api
 * enum {@code org.bukkit.entity.ExperienceOrb.SpawnReason}: this mojmap mixin
 * is applied during Minecraft's Bootstrap (when the NMS {@code ExperienceOrb}
 * class is first loaded through {@code EntityType.<clinit>}), long before the
 * enum type can be injected into the classloader. Storing the ordinal keeps
 * this mixin free of any runtime-missing type reference. The Craft layer
 * ({@code CraftExperienceOrbApiMixin}) maps the ordinal back to the enum, where
 * the enum is already available.</p>
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbFieldsMixin implements ExperienceOrbBridge {

    @Unique
    public java.util.UUID sourceEntityId; // Paper

    @Unique
    public java.util.UUID triggerEntityId; // Paper

    /** Ordinal of {@code SpawnReason.UNKNOWN} (last constant) — default matches Paper. */
    @Unique
    public int spawnReason = 10; // Paper spawnReason (ordinal)

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

    @Override
    public int paper$getSpawnReasonOrdinal() {
        return this.spawnReason;
    }

    @Override
    public void paper$setSpawnReasonOrdinal(int spawnReasonOrdinal) {
        this.spawnReason = spawnReasonOrdinal;
    }
}
