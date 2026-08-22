package dev.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's CompostItemEvent / EntityCompostItemEvent — InputContainer
 * side: when ComposterBlock#addItem returns null (entity event cancelled),
 * skip the levelEvent(1500) + removeItemNoUpdate(0) follow-up.
 */
@Mixin(targets = "net.minecraft.world.level.block.ComposterBlock$InputContainer")
public abstract class ComposterBlockInputContainerMixin {

    @Inject(method = "setChanged",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/block/ComposterBlock;addItem(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/level/block/state/BlockState;"),
            cancellable = true)
    private void paperarc$skipOnCancelledCompost(CallbackInfo ci, @Local(ordinal = 0) BlockState result) {
        if (result == null) {
            ci.cancel();
        }
    }
}
