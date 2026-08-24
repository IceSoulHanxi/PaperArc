package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's CompostItemEvent / EntityCompostItemEvent — InputContainer
 * side: when ComposterBlock#addItem returns null (entity event cancelled),
 * skip the levelEvent(1500) + removeItemNoUpdate(0) follow-up.
 *
 * Implemented loader-neutrally: the addItem call is wrapped so its null result
 * is recorded, and setChanged RETURN cancels when so recorded. This avoids
 * @Local sugars (local shapes differ between fabric's intermediary runtime and
 * NeoForge's patched mojmap jar) — the earlier AFTER-shift + @Local form failed
 * sugar validation on NeoForge.
 */
@Mixin(targets = "net.minecraft.world.level.block.ComposterBlock$InputContainer")
public abstract class ComposterBlockInputContainerMixin {

    @Unique
    private static final ThreadLocal<Boolean> PAPERARC$COMPOST_CANCELLED = new ThreadLocal<>();

    @WrapOperation(method = "setChanged",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/ComposterBlock;addItem(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState paperarc$recordCancelledCompost(Entity entity, BlockState state,
                                                       net.minecraft.world.level.LevelAccessor level,
                                                       net.minecraft.core.BlockPos pos, ItemStack stack,
                                                       Operation<BlockState> original) {
        BlockState result = original.call(entity, state, level, pos, stack);
        PAPERARC$COMPOST_CANCELLED.set(result == null);
        return result;
    }

    @Inject(method = "setChanged", at = @At("RETURN"), cancellable = true)
    private void paperarc$skipOnCancelledCompost(CallbackInfo ci) {
        if (Boolean.TRUE.equals(PAPERARC$COMPOST_CANCELLED.get())) {
            ci.cancel();
        }
        PAPERARC$COMPOST_CANCELLED.remove();
    }
}
