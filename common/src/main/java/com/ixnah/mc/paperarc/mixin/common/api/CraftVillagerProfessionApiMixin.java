package com.ixnah.mc.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B2-6：{@code Villager.Profession extends Translatable} 的终端方法。
 * vanilla 的职业翻译键就是 {@code entity.minecraft.villager.<id>}。
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.entity.CraftVillager$CraftProfession")
public abstract class CraftVillagerProfessionApiMixin {

    @Unique
    public String translationKey() {
        org.bukkit.NamespacedKey key = ((org.bukkit.Keyed) (Object) this).getKey();
        return "entity.minecraft.villager." + key.getKey();
    }
}
