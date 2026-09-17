package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B2-6：{@code Villager.Profession} 在 paper 里 {@code extends Translatable}。
 * 实现体在 {@code api.CraftVillagerProfessionApiMixin}。
 */
@Mixin(targets = "org.bukkit.entity.Villager$Profession", remap = false)
public interface VillagerProfessionIfaceMixin extends net.kyori.adventure.translation.Translatable {

    @Unique
    public abstract String translationKey();
}
