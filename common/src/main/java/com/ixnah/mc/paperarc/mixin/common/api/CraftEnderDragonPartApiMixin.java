package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.entity.CraftEnderDragonPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Damageable#heal(double, RegainReason)} 的第二个实现类：
 * {@code CraftEnderDragonPart} 走 {@code CraftComplexPart} 那条继承链，
 * 拿不到 CraftLivingEntity 上的实现体。
 */
@Mixin(CraftEnderDragonPart.class)
public abstract class CraftEnderDragonPartApiMixin {

    @Unique
    public void heal(double amount, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason reason) {
        com.google.common.base.Preconditions.checkArgument(reason != null, "reason cannot be null");
        org.bukkit.entity.Damageable self = (org.bukkit.entity.Damageable) (Object) this;
        org.bukkit.event.entity.EntityRegainHealthEvent event =
                new org.bukkit.event.entity.EntityRegainHealthEvent(
                        (org.bukkit.entity.Entity) self, amount, reason);
        if (!event.callEvent()) {
            return;
        }
        self.setHealth(Math.min(self.getHealth() + event.getAmount(), self.getMaxHealth()));
    }
}
