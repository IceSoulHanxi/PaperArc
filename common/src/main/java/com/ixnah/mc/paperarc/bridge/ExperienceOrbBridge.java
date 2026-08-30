package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code ExperienceOrb.sourceEntityId} /
 * {@code triggerEntityId} supplementary fields to the api mixins. Paper's patch
 * adds the fields without NMS accessor methods, so the bridge methods carry the
 * {@code paper$} prefix.
 *
 * <p>{@code spawnReason} is deliberately <em>not</em> exposed: its type is the
 * paper-api enum {@code org.bukkit.entity.ExperienceOrb.SpawnReason} which does
 * not exist in the Arclight runtime and crashes mixin application.</p>
 */
public interface ExperienceOrbBridge {

    java.util.UUID paper$getSourceEntityId();

    void paper$setSourceEntityId(java.util.UUID sourceEntityId);

    java.util.UUID paper$getTriggerEntityId();

    void paper$setTriggerEntityId(java.util.UUID triggerEntityId);
}
