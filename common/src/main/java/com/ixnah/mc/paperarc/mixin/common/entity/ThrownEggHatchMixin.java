package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implements Paper's ThrownEggHatchEvent (like PlayerEggThrowEvent, but without
 * the player requirement — dispenser-thrown eggs hatch too).
 * <p>
 * PaperArc mixes into vanilla {@code ThrownEgg#onHit} (the CraftBukkit locals
 * {@code hatching}/{@code b0}/{@code hatchingType} do not exist here), so the
 * whole post-{@code super.onHit} body is taken over with a cancellable inject
 * and reimplemented with Paper's semantics: after the two vanilla hatch rolls
 * (same RNG call order as vanilla), a ThrownEggHatchEvent is fired and its
 * hatching/count/type drive the spawn loop via the Bukkit world API, which
 * carries CreatureSpawnEvent.SpawnReason.EGG. Hatched entities are babies,
 * matching CraftBukkit/Paper behavior.
 */
@Mixin(net.minecraft.world.entity.projectile.ThrownEgg.class)
public abstract class ThrownEggHatchMixin {

    /**
     * NOTE(require=0): Arclight @Overwrites ThrownEgg.onHit entirely (its
     * ThrownEggMixin replaces the vanilla body), so the vanilla super.onHit
     * callsite this anchors on does not exist at runtime. Kept dormant until
     * the overwrite is accounted for.
     */
    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrowableItemProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V",
            shift = At.Shift.AFTER),
        cancellable = true,
        require = 0)
    private void paperarc$thrownEggHatch(HitResult result, CallbackInfo ci) {
        Entity egg = (Entity) (Object) this;
        Level level = egg.level();
        if (!level.isClientSide) {
            if (egg.getRandom().nextInt(8) == 0) {
                // Vanilla calls nextInt(32) unconditionally inside this branch; keep that order.
                byte b0 = (byte) (egg.getRandom().nextInt(32) == 0 ? 4 : 1);

                ThrownEggHatchEvent event = new ThrownEggHatchEvent(
                    (org.bukkit.entity.Egg) PaperArcBridge.bukkitEntity(egg), true, b0,
                    org.bukkit.entity.EntityType.CHICKEN);
                event.callEvent();

                if (event.isHatching()) {
                    int count = event.getNumHatches(); // Paper: hatching ? numHatches : 0
                    if (count > 0) {
                        var world = PaperArcBridge.bukkitWorld((ServerLevel) level);
                        Location location = new Location(world, egg.getX(), egg.getY(), egg.getZ(),
                            egg.getYRot(), 0.0F);
                        Class<? extends org.bukkit.entity.Entity> clazz =
                            event.getHatchingType().getEntityClass();
                        for (int i = 0; i < count; i++) {
                            world.spawn(location, clazz, CreatureSpawnEvent.SpawnReason.EGG,
                                spawned -> {
                                    if (spawned instanceof Ageable ageable) {
                                        ageable.setBaby();
                                    }
                                });
                        }
                    }
                }
            }

            level.broadcastEntityEvent(egg, (byte) 3);
            egg.discard();
        }
        ci.cancel();
    }
}
