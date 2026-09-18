package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code EntityType} 枚举上的 3 个方法（checklist §1.10 am，第 ⑤ 批）。
 * paper 读的是私有字段 {@code key}，这里换成公开的 {@code getKey()}。
 */
@Mixin(EntityType.class)
public abstract class EntityTypeApiMixin {

    @Unique
    private EntityType paperarc$self() {
        return (EntityType) (Object) this;
    }

    @Unique
    public String translationKey() {
        EntityType self = this.paperarc$self();
        Preconditions.checkArgument(self != EntityType.UNKNOWN, "UNKNOWN entities do not have translation keys");
        return Bukkit.getUnsafe().getTranslationKey(self);
    }

    @Unique
    public boolean hasDefaultAttributes() {
        return Bukkit.getUnsafe().hasDefaultEntityAttributes(this.paperarc$self().getKey());
    }

    @Unique
    public Attributable getDefaultAttributes() {
        return Bukkit.getUnsafe().getDefaultEntityAttributes(this.paperarc$self().getKey());
    }
}
