package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.block.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 让 {@code Biome} 实现 adventure 的 {@code Translatable}（checklist §1.12 bb）。
 * 键值照抄 paper（{@code "biome.minecraft." + key.getKey()}）。
 */
@Mixin(Biome.class)
public abstract class BiomeApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "biome.minecraft." + ((Biome) (Object) this).getKey().getKey();
    }
}
