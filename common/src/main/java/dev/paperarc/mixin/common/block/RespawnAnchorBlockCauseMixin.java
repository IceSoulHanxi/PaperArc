package dev.paperarc.mixin.common.block;

import dev.paperarc.mixin.common.player.ServerPlayerSetSpawnMixin;
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cause marker for {@link PlayerSetSpawnEvent}: the respawn anchor path.
 * Marks RESPAWN_ANCHOR for the duration of
 * {@code RespawnAnchorBlock#useWithoutItem}; cleared again at RETURN.
 *
 * Arclight's own RespawnAnchorBlockMixin injects at the
 * {@code setRespawnPosition} INVOKE inside the same method; these hooks sit at
 * HEAD/RETURN instead, so both can coexist (different At points).
 */
@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorBlockCauseMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    private void paperarc$pushAnchorCause(BlockState state, Level world, BlockPos pos, Player player,
                                          BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        dev.paperarc.bridge.SpawnCauseSupport.push(PlayerSetSpawnEvent.Cause.RESPAWN_ANCHOR);
    }

    @Inject(method = "useWithoutItem", at = @At("RETURN"))
    private void paperarc$clearAnchorCause(BlockState state, Level world, BlockPos pos, Player player,
                                           BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        dev.paperarc.bridge.SpawnCauseSupport.clear();
    }
}
