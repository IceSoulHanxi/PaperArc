package com.ixnah.mc.paperarc.mixin.fabric.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.ExperienceOrb;
import org.bukkit.craftbukkit.v.entity.CraftExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fabric-only implementation of ExperienceOrb#getSpawnReason backed by ApiState.
 */
@Mixin(CraftExperienceOrb.class)
public abstract class CraftExperienceOrbSpawnReasonMixin {

    @Unique
    private static final String PAPERARC$KEY_REASON = "orb$spawnReason";

    @Unique
    private ExperienceOrb paperarc$owner() {
        return (ExperienceOrb) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle();
    }

    @Unique
    public org.bukkit.entity.ExperienceOrb.SpawnReason getSpawnReason() {
        return ApiState.get(paperarc$owner(), PAPERARC$KEY_REASON, null);
    }
}
