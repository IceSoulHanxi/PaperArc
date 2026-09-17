package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * RuntimeClassInjector 现在会在 mixin 应用之前把这些 bukkit 类型的字节喂给
 * 字节码 provider（Forge/NeoForge 走 transformerLoader hook，Fabric 由产物直接携带），
 * 描述符可解析，因此本类从 fabric 专属回归 common，三端统一生效（B5）。
 */
@Mixin(targets = "org.bukkit.entity.ExperienceOrb", remap = false)
public interface ExperienceOrbSpawnReasonIfaceMixin {

    @Unique
    public abstract org.bukkit.entity.ExperienceOrb.SpawnReason getSpawnReason();
}
