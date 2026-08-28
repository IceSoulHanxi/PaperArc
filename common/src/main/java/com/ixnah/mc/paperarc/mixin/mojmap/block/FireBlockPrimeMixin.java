package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.ixnah.mc.paperarc.bridge.TNTPrimeState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper 1.20.1 TNTPrimeEvent — FIRE source.
 *
 * <p>Vanilla {@code FireBlock#checkBurnOut} unconditionally calls
 * {@code Level#removeBlock(pos, false)} and then, if the block is TNT,
 * {@code TntBlock.explode(world, pos)}. Paper moves the remove call behind the
 * event (a cancelled prime keeps the block in place and skips the explosion).
 *
 * <p>We anchor on the {@code removeBlock} INVOKE which precedes both the
 * TNT check and the explosion: for a TNT block we fire the event here — a
 * cancelled event aborts the method before the remove and the explode; a
 * passed event lets vanilla continue (remove + funnel into the three-arg
 * explode). The chokepoint {@code TntBlockPrimeMixin} consumes
 * {@link TNTPrimeState} so the funnel does not produce a second, bogus
 * REDSTONE event.
 */
@Mixin(FireBlock.class)
public abstract class FireBlockPrimeMixin {

    @Inject(method = "checkBurnOut",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"),
            cancellable = true)
    private void paperarc$primeFire(Level level, BlockPos pos, int chance, RandomSource random, int age,
                                    CallbackInfo ci) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TntBlock)) {
            return;
        }
        if (!new TNTPrimeEvent(CraftBlock.at(level, pos), TNTPrimeEvent.PrimeReason.FIRE, null).callEvent()) {
            ci.cancel();
            return;
        }
        // vanilla continues: removeBlock + explode(2-arg) -> funnel 3-arg.
        // Tell the chokepoint mixin not to report a duplicate event.
        TNTPrimeState.setFirePrime(true);
    }
}