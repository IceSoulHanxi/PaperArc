package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * Shared fire logic for the EntityKnockbackByEntityEvent port (1.20.1).
 * See {@code PlayerKnockbackMixin} / {@code MobKnockbackMixin} for the call sites.
 */
public final class EntityKnockbackByEntityEventHelper {

    private EntityKnockbackByEntityEventHelper() {
    }

    static void fire(LivingEntity target, double strength, double x, double z,
                     Entity attacker, Operation<Void> original) {
        org.bukkit.entity.LivingEntity bukkitTarget =
                (org.bukkit.entity.LivingEntity) PaperArcBridge.bukkitEntity(target);
        // Paper passes the raw strength and the (x,0,z) direction delta to the event;
        // the event only supports cancellation in this 1.20.1 API (getAcceleration has no setter).
        double applied = strength * (1.0D - target.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE));
        Vector delta = new Vector(x, 0.0D, z).normalize().multiply(applied);
        EntityKnockbackByEntityEvent event = new EntityKnockbackByEntityEvent(
                bukkitTarget, PaperArcBridge.bukkitEntity(attacker), (float) strength, delta);
        if (event.callEvent()) {
            original.call(target, strength, x, z);
        }
    }
}
