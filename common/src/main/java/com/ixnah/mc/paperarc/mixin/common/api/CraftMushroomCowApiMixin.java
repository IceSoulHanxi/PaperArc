package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.animal.MushroomCow;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftMushroomCow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's missing-entity-behaviour API {@code MushroomCow} stew methods to
 * {@link CraftMushroomCow}.
 *
 * <p>{@code effect} (f_28909_) and {@code effectDuration} (f_28910_) are private fields
 * widened via AT. {@code MobEffect.byId(int)} / {@code MobEffect.getId(MobEffect)} bridge
 * to bukkit {@code PotionEffectType} (deprecated {@code getById}/{@code getId} are used to
 * mirror Paper's implementation).</p>
 */
@Mixin(CraftMushroomCow.class)
public abstract class CraftMushroomCowApiMixin {

    @Shadow
    public abstract MushroomCow getHandle();

    @Unique
    public int getStewEffectDuration() {
        return this.getHandle().effectDuration;
    }

    @Unique
    public void setStewEffectDuration(int duration) {
        this.getHandle().effectDuration = duration;
    }

    @Unique
    public org.bukkit.potion.PotionEffectType getStewEffectType() {
        MobEffect effect = this.getHandle().effect;
        if (effect == null) {
            return null;
        }
        return org.bukkit.potion.PotionEffectType.getById(MobEffect.getId(effect));
    }

    @Unique
    public void setStewEffect(org.bukkit.potion.PotionEffectType type) {
        MobEffect effect = null;
        if (type != null) {
            effect = MobEffect.byId(type.getId());
        }
        this.getHandle().effect = effect;
    }
}
