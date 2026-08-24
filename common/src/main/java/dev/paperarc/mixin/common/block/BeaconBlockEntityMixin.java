package dev.paperarc.mixin.common.block;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's BlockLockCheckEvent for the beacon path
 * (Add-BlockLockCheckEvent.patch also rewrites
 * BeaconBlockEntity#createMenu to use the event-aware canUnlock overload).
 *
 * We replicate the vanilla createMenu body but route the lock check through
 * BaseContainerBlockEntityMixin#paperarc$canUnlockWithEvent; the lock code is
 * reached via {@link LockCodeAccessor} (private field of the parent class).
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {

    @Shadow
    @Final
    private ContainerData dataAccess;

    @Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
    private void paperarc$blockLockCheck(int syncId, Inventory playerInventory, Player player,
                                         CallbackInfoReturnable<AbstractContainerMenu> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return; // matches Paper: only ServerPlayer goes through the event
        }
        BeaconBlockEntity blockEntity = (BeaconBlockEntity) (Object) this;
        if (blockEntity.getLevel() == null
                || blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            return; // matches Paper: fall back to vanilla logic
        }
        LockCode lock = ((LockCodeAccessor) blockEntity).paperarc$getLockKey();
        Component containerName = ((BeaconBlockEntity) blockEntity).getDisplayName();
        if (dev.paperarc.bridge.ContainerUnlockSupport.canUnlockWithEvent(serverPlayer, lock, containerName, blockEntity)) {
            cir.setReturnValue(new BeaconMenu(syncId, playerInventory, this.dataAccess,
                    ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())));
        } else {
            cir.setReturnValue(null);
        }
    }
}
