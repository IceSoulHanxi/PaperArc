package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.entity.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 让 {@code Villager.Profession} 实现 adventure {@code Translatable}（A6/X-2 第三批）。 */
@Mixin(Villager.Profession.class)
public abstract class VillagerProfessionApiMixin implements Translatable {

    @Unique
    public String translationKey() {
        return "entity.minecraft.villager." + ((Villager.Profession) (Object) this).getKey().getKey();
    }
}
