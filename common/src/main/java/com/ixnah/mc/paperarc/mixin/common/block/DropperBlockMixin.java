package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.sugar.Local;
import io.papermc.paper.event.block.BlockFailedDispenseEvent;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's BlockFailedDispenseEvent + BlockPreDispenseEvent for
 * DropperBlock#dispenseFrom (DropperBlock overrides DispenserBlock's method,
 * so the patches touch it separately — same semantics as DispenserBlockMixin).
 */
@Mixin(DropperBlock.class)
public abstract class DropperBlockMixin {

    @Inject(method = "dispenseFrom",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"),
            cancellable = true)
    private void paperarc$failedDispense(ServerLevel world, BlockState state, BlockPos pos, CallbackInfo ci) {
        if (!new BlockFailedDispenseEvent(CraftBlock.at(world, pos)).callEvent()) {
            ci.cancel();
        }
    }

    @Inject(method = "dispenseFrom",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true)
    private void paperarc$preDispense(ServerLevel world, BlockState state, BlockPos pos, CallbackInfo ci,
                                      @Local(ordinal = 0) int slot, @Local(ordinal = 0) ItemStack stack) {
        // Only reachable in Paper's `iinventory == null` branch: the direct-dispense
        // path is the only dispense() call in the method.
        BlockPreDispenseEvent event =
                new BlockPreDispenseEvent(CraftBlock.at(world, pos), CraftItemStack.asCraftMirror(stack), slot);
        if (!event.callEvent()) {
            ci.cancel();
        }
    }
}
