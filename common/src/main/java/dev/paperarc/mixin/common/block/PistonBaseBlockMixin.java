package dev.paperarc.mixin.common.block;

import dev.paperarc.bridge.BlockBreakBlockEventSupport;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-BlockBreakBlockEvent.patch (piston part).
 *
 * <p>Paper swaps the {@code dropResources(state, world, destroyedPos, blockEntity)}
 * call in {@code PistonBaseBlock#moveBlocks} for the 5-arg overload carrying the
 * piston position as event source. We wrap that invocation and route through
 * {@link BlockBreakBlockEventSupport#breakWithEvent}.
 */
@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {

    @WrapOperation(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    // runtime CP owner is PistonBaseBlock itself (unqualified inherited-static call);
                    // intermediary literal bypasses refmap (tiny cannot map this site)
                    target = "Lnet/minecraft/class_2665;method_9610(Lnet/minecraft/class_2680;Lnet/minecraft/class_1936;Lnet/minecraft/class_2338;Lnet/minecraft/class_2586;)V",
                    remap = false
            )
    )
    private void paperarc$pistonBreakWithEvent(BlockState state, LevelAccessor accessor, BlockPos destroyedPos,
                                               BlockEntity blockEntity, Operation<Void> original,
                                               @Local(argsOnly = true) BlockPos sourcePos) {
        BlockBreakBlockEventSupport.breakWithEvent(state, accessor, destroyedPos, blockEntity, sourcePos);
    }
}
