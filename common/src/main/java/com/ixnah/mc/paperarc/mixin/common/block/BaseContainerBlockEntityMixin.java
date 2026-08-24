package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.ContainerUnlockSupport;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's BlockLockCheckEvent
 * (Add-BlockLockCheckEvent.patch).
 *
 * Paper adds a static canUnlock(Player, LockCode, Component, BlockEntity)
 * overload that fires the event for ServerPlayers on a valid block entity and
 * routes canOpen through it. Since we cannot add overloads, we inject into
 * {@code canOpen} at HEAD and replicate the Paper logic; the shared logic
 * lives in {@link ContainerUnlockSupport} so BeaconBlockEntityMixin can
 * reuse it (see BeaconBlockEntityMixin for the beacon path).
 */
@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin {

    @Shadow
    private LockCode lockKey;

    @Inject(method = "canOpen", at = @At("HEAD"), cancellable = true)
    private void paperarc$blockLockCheck(Player player, CallbackInfoReturnable<Boolean> cir) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        Level level = blockEntity.getLevel();
        if (!(player instanceof ServerPlayer serverPlayer) || level == null
                || level.getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            return; // matches Paper: fall back to vanilla logic
        }
        cir.setReturnValue(ContainerUnlockSupport.canUnlockWithEvent(
                serverPlayer, this.lockKey, ((BaseContainerBlockEntity) (Object) this).getDisplayName(), blockEntity));
    }
}
