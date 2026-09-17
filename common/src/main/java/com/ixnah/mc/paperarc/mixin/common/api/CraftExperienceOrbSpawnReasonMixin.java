package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.ExperienceOrb;
import org.bukkit.craftbukkit.v.entity.CraftExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * RuntimeClassInjector 现在会在 mixin 应用之前把这些 bukkit 类型的字节喂给
 * 字节码 provider（Forge/NeoForge 走 transformerLoader hook，Fabric 由产物直接携带），
 * 描述符可解析，因此本类从 fabric 专属回归 common，三端统一生效（B5）。
 */
@Mixin(CraftExperienceOrb.class)
public abstract class CraftExperienceOrbSpawnReasonMixin {

    @Unique
    private ExperienceOrb paperarc$owner() {
        return (ExperienceOrb) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle();
    }

    /**
     * spawnReason 存在 NMS 侧补充字段里（{@code ExperienceOrbFieldsMixin} 注入），
     * 以序数保存 —— 该枚举类型是运行时注入的（B5），字段类型写枚举会在
     * FieldsMixin 加载时把它拖进来，时序上过早。
     */
    @Unique
    public org.bukkit.entity.ExperienceOrb.SpawnReason getSpawnReason() {
        int ordinal = ((com.ixnah.mc.paperarc.bridge.ExperienceOrbBridge) paperarc$owner())
                .paper$getSpawnReasonOrdinal();
        org.bukkit.entity.ExperienceOrb.SpawnReason[] values =
                org.bukkit.entity.ExperienceOrb.SpawnReason.values();
        return ordinal < 0 || ordinal >= values.length ? null : values[ordinal];
    }
}
