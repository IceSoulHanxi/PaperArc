package dev.paperarc.mixin.fabric.block;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import net.minecraft.core.BlockPos;
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
 * Port of Paper's TNTPrimeEvent for FireBlock (Add-TNTPrimeEvent.patch).
 *
 * Paper guards the burn-out of a TNT block with a FIRE-reason event; on
 * cancel nothing happens (no priming, no fire spread, no block removal).
 * We inject right before the static TntBlock#explode(Level, BlockPos) call
 * at the tail of checkBurnOut (only reachable when the burned block is TNT)
 * and cancel the rest of the method when the event is cancelled.
 *
 * NOTE: runtime server is VANILLA 1.21.1 (fabric intermediary): vanilla
 * checkBurnOut primes TNT via static explode; the NeoForge-style
 * BlockState#onCaughtFire hook seen in the compile-time merged jar does NOT
 * exist at runtime — anchoring on it silently misses.
 *
 * Conflict notes vs Arclight: FireBlockMixin only redirects setBlock/
 * removeBlock in tick() and defaultBlockState in updateShape(); checkBurnOut
 * is unoccupied.
 */
@Mixin(FireBlock.class)
public abstract class FireBlockPrimeMixin {

    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"),
            cancellable = true)
    private void paperarc$primeFire(Level level, BlockPos pos, int chance, RandomSource random, int age,
                                    CallbackInfo ci) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TntBlock)) {
            return;
        }
        if (!new TNTPrimeEvent(CraftBlock.at(level, pos), TNTPrimeEvent.PrimeReason.FIRE, null).callEvent()) {
            ci.cancel();
        }
    }
}
