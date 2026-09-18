package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalByteRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's {@code Add-ThrownEggHatchEvent.patch}.
 *
 * <p>Paper fires {@link ThrownEggHatchEvent} in the <b>else</b> branch of the
 * CraftBukkit {@code if (shooter instanceof ServerPlayer)} block inside
 * {@code ThrownEgg#onHit}: a player-thrown egg keeps firing the legacy
 * {@code PlayerEggThrowEvent}, everything else (dispenser, mob) gets the Paper
 * event. Both events drive the same three values — {@code hatching},
 * {@code numHatches}, {@code hatchingType}.
 *
 * <p>Arclight {@code @Overwrite}s {@code ThrownEgg#onHit} with exactly that
 * CraftBukkit shape, so we do not take the body over (the previous attempt did,
 * and anchored on a call site that does not survive the overwrite — see below).
 * Instead we hook the single {@code GETSTATIC EntityType.CHICKEN} that seeds
 * {@code hatchingType} and, when the shooter is not a player, fire the Paper
 * event there: {@code hatching} (slot 2) and {@code numHatches} (slot 3) are
 * already assigned at that point, and the {@code hatchingType} slot is written
 * from our return value. The subsequent {@code instanceof ServerPlayer} branch
 * is skipped by definition, so the two events never both fire.
 *
 * <p><b>Why the old anchor failed (Y-1).</b> The deleted version injected after
 * {@code ThrowableItemProjectile#onHit} — the {@code super.onHit(…)} call in
 * vanilla {@code ThrownEgg}. Arclight's {@code ThrownEggMixin} extends
 * {@code ThrowableProjectileMixin}, which targets {@code Projectile}, so Mixin
 * rewrites that super call to {@code Projectile#onHit} when the overwrite is
 * merged. The literal {@code ThrowableItemProjectile#onHit} is simply absent
 * from the applied class ({@code Scanned 0}). It was never an ordering problem:
 * {@code paperarc-common.mixins.json} runs at priority 1100 against Arclight's
 * 500, so our injectors already see Arclight's finished bytecode.
 */
@Mixin(ThrownEgg.class)
public abstract class ThrownEggHatchMixin {

    @ModifyExpressionValue(
            method = "onHit(Lnet/minecraft/world/phys/HitResult;)V",
            at = @At(
                    value = "FIELD",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC,
                    target = "Lorg/bukkit/entity/EntityType;CHICKEN:Lorg/bukkit/entity/EntityType;",
                    remap = false
            )
    )
    private EntityType paperarc$fireThrownEggHatchEvent(EntityType hatchingType,
                                                        @Local(index = 2) LocalBooleanRef hatching,
                                                        @Local(index = 3) LocalByteRef numHatches) {
        ThrownEgg egg = (ThrownEgg) (Object) this;
        Entity shooter = egg.getOwner();
        if (shooter instanceof ServerPlayer) {
            // CraftBukkit/Arclight fires PlayerEggThrowEvent for this case, same as Paper.
            return hatchingType;
        }
        ThrownEggHatchEvent event = new ThrownEggHatchEvent(
                (Egg) PaperArcBridge.bukkitEntity(egg), hatching.get(), numHatches.get(), hatchingType);
        event.callEvent();
        hatching.set(event.isHatching());
        numHatches.set(event.getNumHatches());
        return event.getHatchingType();
    }
}
