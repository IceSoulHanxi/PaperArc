package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.sugar.Local;
import io.papermc.paper.event.block.BlockFailedDispenseEvent;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's BlockFailedDispenseEvent + BlockPreDispenseEvent
 * (Add-BlockFailedDispenseEvent.patch / Add-BlockPreDispenseEvent.patch)
 * for DispenserBlock#dispenseFrom.
 *
 * - Failed: Paper wraps the empty-inventory branch (levelEvent(1001) +
 *   gameEvent) in handleBlockFailedDispenseEvent; we inject cancellable at
 *   the levelEvent call so cancelling skips both effects.
 * - Pre: Paper fires before behavior#dispense and returns when cancelled; we
 *   inject cancellable at the dispense call (cancelling also skips setItem).
 *   Avoids Arclight's existing @Inject at the setItem callsite.
 */
@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {

    @Inject(method = "dispenseFrom",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"),
            cancellable = true)
    private void paperarc$failedDispense(ServerLevel world, BlockPos pos, CallbackInfo ci) {
        if (!new BlockFailedDispenseEvent(CraftBlock.at(world, pos)).callEvent()) {
            ci.cancel();
        }
    }

    @Inject(method = "dispenseFrom",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true)
    private void paperarc$preDispense(ServerLevel world, BlockPos pos, CallbackInfo ci,
                                      @Local(ordinal = 0) int slot, @Local(ordinal = 0) ItemStack stack) {
        BlockPreDispenseEvent event =
                new BlockPreDispenseEvent(CraftBlock.at(world, pos), CraftItemStack.asCraftMirror(stack), slot);
        if (!event.callEvent()) {
            ci.cancel();
        }
    }
}
