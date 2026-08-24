package dev.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's knockback events (Add-entity-knockback-events.patch).
 *
 * <p>Paper replaces CraftBukkit's {@code callEntityKnockbackEvent} so every knockback funnels
 * through the CraftBukkit-injected overload
 * {@code LivingEntity#knockback(double,double,double,Entity,EntityKnockbackEvent.KnockbackCause)}
 * firing both the legacy bukkit event and the new paper events. That overload does not exist in
 * the vanilla (loom) jar we compile against but exists at runtime under Arclight (Arclight's own
 * LivingEntityMixin re-declares it as a delegation shim), so this mixin targets it via its exact
 * runtime descriptor, cancels the original body and replicates Paper's math + dual-event flow.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityKnockbackMixin {

    @Inject(
            method = "knockback(DDLnet/minecraft/world/entity/Entity;Lorg/bukkit/event/entity/EntityKnockbackEvent$KnockbackCause;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void paperarc$knockbackEvents(double strength, double x, double z, Entity attacker,
                                          org.bukkit.event.entity.EntityKnockbackEvent.KnockbackCause cause,
                                          CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        org.bukkit.entity.LivingEntity bukkitSelf =
                (org.bukkit.entity.LivingEntity) PaperArcBridge.bukkitEntity(self);

        double force = strength * (1.0D - self.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 current = self.getDeltaMovement();
        Vec3 push = new Vec3(x, 0.0D, z).normalize().scale(force);
        // Paper: final velocity = half the current motion minus the knockback push,
        // with a capped vertical component while on ground.
        Vec3 finalVelocity = new Vec3(
                current.x / 2.0D - push.x,
                self.onGround() ? Math.min(0.4D, current.y / 2.0D + force) : current.y,
                current.z / 2.0D - push.z);
        Vec3 diff = finalVelocity.subtract(current);

        Vector apiKnockback = new Vector(diff.x, diff.y, diff.z);
        Vector currentVelocity = new Vector(current.x, current.y, current.z);

        // Legacy bukkit event (mirrors Paper's replacement of CraftEventFactory.callEntityKnockbackEvent)
        Vector legacyFinalKnockback = currentVelocity.clone().add(apiKnockback);
        org.bukkit.event.entity.EntityKnockbackEvent.KnockbackCause legacyCause =
                org.bukkit.event.entity.EntityKnockbackEvent.KnockbackCause.valueOf(cause.name());
        org.bukkit.event.entity.EntityKnockbackEvent legacyEvent;
        if (attacker != null) {
            legacyEvent = new org.bukkit.event.entity.EntityKnockbackByEntityEvent(
                    bukkitSelf, PaperArcBridge.bukkitEntity(attacker), legacyCause, force, apiKnockback, legacyFinalKnockback);
        } else {
            legacyEvent = new org.bukkit.event.entity.EntityKnockbackEvent(
                    bukkitSelf, legacyCause, force, apiKnockback, legacyFinalKnockback);
        }
        legacyEvent.callEvent();

        apiKnockback = legacyEvent.getFinalKnockback().subtract(currentVelocity);

        EntityKnockbackEvent.Cause paperCause = EntityKnockbackEvent.Cause.valueOf(cause.name());
        EntityKnockbackEvent paperEvent;
        if (attacker != null) {
            paperEvent = new EntityKnockbackByEntityEvent(
                    bukkitSelf, PaperArcBridge.bukkitEntity(attacker), paperCause, (float) force, apiKnockback);
        } else {
            paperEvent = new EntityKnockbackEvent(bukkitSelf, paperCause, apiKnockback);
        }
        paperEvent.setCancelled(legacyEvent.isCancelled());
        paperEvent.callEvent();

        if (paperEvent.isCancelled()) {
            ci.cancel();
            return;
        }

        Vector knockback = paperEvent.getKnockback();
        self.setDeltaMovement(current.add(knockback.getX(), knockback.getY(), knockback.getZ()));
        self.hasImpulse = true;
        ci.cancel();
    }
}
