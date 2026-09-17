package com.ixnah.mc.paperarc.mixin.common.item;

import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.ProjectileLaunchSupport;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent launch point for {@link EggItem}
 * (patches/server/PlayerLaunchProjectileEvent.patch).
 *
 * <p>Vanilla 1.20.1 {@code EggItem#use} contains exactly one
 * {@code Level#addFreshEntity} and one {@code ItemStack#shrink(1)} call
 * (javap-verified), so the event gate and the {@code shouldConsume()} handling
 * each need a single wrap.
 *
 * <p>Deviation from Paper: Paper moves {@code awardStat} inside the success
 * branch; here the vanilla stat award still runs on a cancelled launch.
 */
@Mixin(EggItem.class)
public abstract class EggItemMixin {

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean paperarc$launch(Level world, Entity projectile, Operation<Boolean> original,
                                    Level level, Player user, InteractionHand hand) {
        if (world.isClientSide) {
            return original.call(world, projectile);
        }
        return ProjectileLaunchSupport.callLaunchEvent(user, user.getItemInHand(hand), projectile)
                && original.call(world, projectile);
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    )
    private void paperarc$consume(ItemStack stack, int amount, Operation<Void> original,
                                  Level level, Player user, InteractionHand hand) {
        if (ProjectileLaunchSupport.suppressConsume()) {
            if (!LaunchState.isCancelled()) {
                // cancelled launches re-send the inventory from paperarc$result instead
                ProjectileLaunchSupport.updateInventory(user);
            }
            return;
        }
        original.call(stack, amount);
    }

    @ModifyReturnValue(method = "use", at = @At("RETURN"))
    private InteractionResultHolder<ItemStack> paperarc$result(InteractionResultHolder<ItemStack> original,
                                                               Level level, Player user, InteractionHand hand) {
        if (LaunchState.takeCancelled()) {
            ProjectileLaunchSupport.updateInventory(user);
            return InteractionResultHolder.fail(user.getItemInHand(hand));
        }
        return original;
    }
}
