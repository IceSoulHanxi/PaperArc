package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code ExperienceOrb.sourceEntityId} /
 * {@code triggerEntityId} supplementary fields and the {@code spawnReason}
 * ordinal to the api mixins. Paper's patch adds the fields without NMS
 * accessor methods, so the bridge methods carry the {@code paper$} prefix.
 *
 * <p>{@code spawnReason} is exposed as an {@code int} ordinal (not the
 * paper-api enum type) so the NMS-side mixin never references a class that
 * does not exist in the Arclight runtime during Minecraft's Bootstrap. The
 * Craft layer maps the ordinal back to
 * {@code org.bukkit.entity.ExperienceOrb.SpawnReason}.</p>
 */
public interface ExperienceOrbBridge {

    java.util.UUID paper$getSourceEntityId();

    void paper$setSourceEntityId(java.util.UUID sourceEntityId);

    java.util.UUID paper$getTriggerEntityId();

    void paper$setTriggerEntityId(java.util.UUID triggerEntityId);

    int paper$getSpawnReasonOrdinal();

    void paper$setSpawnReasonOrdinal(int spawnReasonOrdinal);
}
