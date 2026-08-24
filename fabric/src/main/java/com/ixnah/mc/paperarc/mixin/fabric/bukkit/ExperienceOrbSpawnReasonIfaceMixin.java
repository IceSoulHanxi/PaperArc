package com.ixnah.mc.paperarc.mixin.fabric.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fabric-only declaration of Paper's ExperienceOrb spawn-reason surface.
 * Arclight's stripped bukkit lacks the SpawnReason inner class on NeoForge,
 * where the descriptor would fail early mixin apply.
 */
@Mixin(targets = "org.bukkit.entity.ExperienceOrb", remap = false)
public interface ExperienceOrbSpawnReasonIfaceMixin {

    @Unique
    public abstract org.bukkit.entity.ExperienceOrb.SpawnReason getSpawnReason();
}
