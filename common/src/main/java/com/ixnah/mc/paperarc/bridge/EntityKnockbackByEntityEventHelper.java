package com.ixnah.mc.paperarc.bridge;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
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

    public static void fire(LivingEntity target, double strength, double x, double z,
                     Entity attacker, Operation<Void> original) {
        org.bukkit.entity.LivingEntity bukkitTarget =
                (org.bukkit.entity.LivingEntity) PaperArcBridge.bukkitEntity(target);
        // Paper passes the raw strength and the (x,0,z) direction delta to the event.
        // 1.21.1 的构造器多了一个 EntityKnockbackEvent.Cause（1.20.1 没有）——
        // 这条路径是"被实体攻击导致的击退"，对应 Cause.ENTITY_ATTACK。
        double applied = strength * (1.0D - target.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE));
        Vector delta = new Vector(x, 0.0D, z).normalize().multiply(applied);
        EntityKnockbackByEntityEvent event = new EntityKnockbackByEntityEvent(
                bukkitTarget, PaperArcBridge.bukkitEntity(attacker),
                io.papermc.paper.event.entity.EntityKnockbackEvent.Cause.ENTITY_ATTACK,
                (float) strength, delta);
        if (event.callEvent()) {
            original.call(target, strength, x, z);
        }
    }
}
