package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge twin of the fabric FireBlockPrimeMixin (Paper TNTPrimeEvent on
 * fire-caused priming).
 *
 * Fabric's vanilla runtime primes TNT via the static TntBlock#explode call at
 * the tail of checkBurnOut. The NeoForge pipeline replaces that call with
 * BlockState#onCaughtFire(Level, BlockPos, Direction, LivingEntity) — so this
 * twin anchors there instead and cancels the burn-out when Paper's event is
 * cancelled.
 */
@Mixin(FireBlock.class)
public abstract class FireBlockPrimeMixin {

    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;onCaughtFire(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/entity/LivingEntity;)V",
            remap = false),
            cancellable = true)
    private void paperarc$primeFire(Level level, BlockPos pos, int chance, RandomSource random, int age,
                                    net.minecraft.core.Direction direction, CallbackInfo ci) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TntBlock)) {
            return;
        }
        if (!new TNTPrimeEvent(CraftBlock.at(level, pos), TNTPrimeEvent.PrimeReason.FIRE, null).callEvent()) {
            ci.cancel();
        }
    }
}
