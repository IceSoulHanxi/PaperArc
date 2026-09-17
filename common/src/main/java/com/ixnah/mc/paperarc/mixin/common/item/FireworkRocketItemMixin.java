package com.ixnah.mc.paperarc.mixin.common.item;

import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.ProjectileLaunchSupport;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent launch point for firework rockets
 * (patches/server/PlayerLaunchProjectileEvent.patch -> FireworkRocketItem#useOn).
 *
 * <p>Only {@code useOn} is patched by Paper: that is the "place a rocket on a
 * block" path which spawns the entity. {@code use} (elytra boost) consumes the
 * rocket through a different code path and Paper leaves it alone.
 *
 * <p>Vanilla 1.20.1 {@code useOn} has exactly one {@code Level#addFreshEntity}
 * and one {@code ItemStack#shrink(1)} (javap-verified). A cancelled event maps
 * to Paper's {@code InteractionResult.PASS}.
 */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {

    @WrapOperation(
            method = "useOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean paperarc$launch(Level world, Entity rocket, Operation<Boolean> original, UseOnContext context) {
        Player player = context.getPlayer();
        if (world.isClientSide || player == null) {
            return original.call(world, rocket);
        }
        return ProjectileLaunchSupport.callLaunchEvent(player, context.getItemInHand(), rocket)
                && original.call(world, rocket);
    }

    @WrapOperation(
            method = "useOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    )
    private void paperarc$consume(ItemStack stack, int amount, Operation<Void> original, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && ProjectileLaunchSupport.suppressConsume()) {
            if (!LaunchState.isCancelled()) {
                ProjectileLaunchSupport.updateInventory(player);
            }
            return;
        }
        original.call(stack, amount);
    }

    @ModifyReturnValue(method = "useOn", at = @At("RETURN"))
    private InteractionResult paperarc$result(InteractionResult original, UseOnContext context) {
        if (LaunchState.takeCancelled()) {
            Player player = context.getPlayer();
            if (player != null) {
                ProjectileLaunchSupport.updateInventory(player);
            }
            return InteractionResult.PASS;
        }
        return original;
    }
}
