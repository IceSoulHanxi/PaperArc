package dev.paperarc.mixin.common.entity;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.minecraft.world.entity.Entity;
import org.bukkit.util.Vector;

/**
 * Shared logic replicating Paper's {@code Entity#push(double,double,double,Entity)} overload
 * (Add-entity-knockback-events.patch): fires {@link EntityPushedByEntityAttackEvent} with
 * {@code Cause.PUSH} and returns the (possibly modified) knockback vector, or {@code null} when
 * the event was cancelled (caller must skip the push entirely).
 */
public final class PushByEntityEvents {

    private PushByEntityEvents() {
    }

    public static double[] fire(Entity pushed, Entity pushingEntity, double x, double y, double z) {
        Vector delta = new Vector(x, y, z);
        EntityPushedByEntityAttackEvent event = new EntityPushedByEntityAttackEvent(
                PaperArcBridge.bukkitEntity(pushed),
                io.papermc.paper.event.entity.EntityKnockbackEvent.Cause.PUSH,
                PaperArcBridge.bukkitEntity(pushingEntity),
                delta);
        if (!event.callEvent()) {
            return null;
        }
        Vector knockback = event.getKnockback();
        return new double[]{knockback.getX(), knockback.getY(), knockback.getZ()};
    }
}
