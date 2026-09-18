package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 加在 {@code EntityType} 上的方法与 adventure {@code Translatable}（A6/X-2 第三批）。 */
@Mixin(EntityType.class)
public abstract class EntityTypeApiMixin implements Translatable {

    @Unique
    private EntityType paperarc$self() {
        return (EntityType) (Object) this;
    }

    @Unique
    public String translationKey() {
        return Bukkit.getUnsafe().getTranslationKey(this.paperarc$self());
    }

    @Unique
    public boolean hasDefaultAttributes() {
        return Bukkit.getUnsafe().hasDefaultEntityAttributes(this.paperarc$self().getKey());
    }

    @Unique
    public org.bukkit.attribute.Attributable getDefaultAttributes() {
        return Bukkit.getUnsafe().getDefaultEntityAttributes(this.paperarc$self().getKey());
    }
}
