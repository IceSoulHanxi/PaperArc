package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.ixnah.mc.paperarc.bridge.TNTPrimeState;
import net.minecraft.world.level.block.TntBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

/**
 * NeoForge twin of the fabric TntBlockPrimeMixin (Paper TNTPrimeEvent).
 *
 * Fabric keeps the four vanilla prime paths (onPlace / neighborChanged /
 * onProjectileHit / wasExploded) calling the two-arg static explode, so each
 * path is wrapped separately with an exact reason. The NeoForge pipeline funnels
 * every prime through the private three-arg
 * {@code explode(Level, BlockPos, LivingEntity)} instead (onCaughtFire extension
 * included), and the original reason is not carried into it.
 *
 * Reason heuristic at this chokepoint:
 *  - a living-entity argument maps to PROJECTILE (projectile hits pass their
 *    owner/shooter),
 *  - everything else defaults to REDSTONE (the dominant no-entity path:
 *    redstone priming and fire share the null-entity form here; FIRE is still
 *    reported precisely by our FireBlockPrimeMixin twin upstream).
 */
@Mixin(TntBlock.class)
public abstract class TntBlockPrimeMixin {

    @Inject(method = "explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void paperarc$prime(Level level, BlockPos pos, @Nullable LivingEntity entity,
                                       CallbackInfo ci) {
        // FIRE primes already reported by FireBlockPrimeMixin funnel through
        // here (two-arg explode -> three-arg explode); skip the duplicate.
        if (TNTPrimeState.takeFirePrime()) {
            return;
        }
        TNTPrimeEvent.PrimeReason reason = entity != null
                ? TNTPrimeEvent.PrimeReason.PROJECTILE
                : TNTPrimeEvent.PrimeReason.REDSTONE;
        if (!new TNTPrimeEvent(CraftBlock.at(level, pos), reason, PaperArcBridge.bukkitEntity(entity)).callEvent()) {
            ci.cancel();
        }
    }
}
