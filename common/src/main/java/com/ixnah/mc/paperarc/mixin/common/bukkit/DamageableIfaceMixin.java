package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Damageable}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 *
 * <p>注意别和 {@code org.bukkit.inventory.meta.Damageable} 搞混 —— 两者简单名相同，
 * 后者的声明在 {@link ItemMetaDamageableIfaceMixin}。</p>
 */
@Mixin(targets = "org.bukkit.entity.Damageable", remap = false)
public interface DamageableIfaceMixin {

    @Unique
    public abstract void heal(double p0, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason p1);
}
