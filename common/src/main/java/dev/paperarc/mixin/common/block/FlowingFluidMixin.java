package dev.paperarc.mixin.common.block;

import dev.paperarc.bridge.BlockBreakBlockEventSupport;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-BlockBreakBlockEvent.patch (fluid part).
 *
 * <p>Paper routes the destroyed-block drop through a new
 * {@code beforeDestroyingBlock(world, pos, state, source)} overload where
 * {@code source} is {@code pos.relative(direction.getOpposite())} from
 * {@code spreadTo}; only {@link WaterFluid} overrides it to fire
 * {@code BlockBreakBlockEvent} (lava keeps vanilla behaviour). We wrap the
 * {@code beforeDestroyingBlock} invocation in {@code spreadTo} and branch on
 * the fluid type, replicating exactly that.
 */
@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {

    @WrapOperation(
            method = "spreadTo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FlowingFluid;beforeDestroyingBlock(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    private void paperarc$beforeDestroyingBlockWithSource(FlowingFluid instance, LevelAccessor accessor, BlockPos pos,
                                                          BlockState state, Operation<Void> original,
                                                          @Local(argsOnly = true) Direction direction) {
        if ((Object) instance instanceof WaterFluid) {
            BlockBreakBlockEventSupport.breakWithEvent(
                    state,
                    accessor,
                    pos,
                    state.hasBlockEntity() ? accessor.getBlockEntity(pos) : null,
                    pos.relative(direction.getOpposite()));
        } else {
            original.call(instance, accessor, pos, state);
        }
    }
}
