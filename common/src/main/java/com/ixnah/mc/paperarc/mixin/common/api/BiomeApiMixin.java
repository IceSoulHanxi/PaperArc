package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.block.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 让 {@code Biome} 实现 adventure {@code Translatable}（A6/X-2 第三批）。 */
@Mixin(Biome.class)
public abstract class BiomeApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "biome.minecraft." + ((Biome) (Object) this).getKey().getKey();
    }
}
