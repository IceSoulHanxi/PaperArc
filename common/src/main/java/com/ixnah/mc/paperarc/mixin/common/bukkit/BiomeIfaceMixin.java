package com.ixnah.mc.paperarc.mixin.common.bukkit;

import net.kyori.adventure.translation.Translatable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code Biome} 实现 adventure 的 {@code Translatable}（checklist §1.12 bb）。
 * 键值照抄 paper-api 的 default 方法（{@code "biome.minecraft." + key.getKey()}）。
 *
 * <p>1.21.11 起运行时的 {@code Biome} 是接口（{@code OldEnum}），改成 interface mixin。
 */
@Mixin(targets = "org.bukkit.block.Biome", remap = false)
public interface BiomeIfaceMixin extends Translatable {

    @Unique
    public default String translationKey() {
        return "biome.minecraft." + ((org.bukkit.block.Biome) this).getKey().getKey();
    }
}
