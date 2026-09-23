package com.ixnah.mc.paperarc.mixin.common.item;

import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.ProjectileLaunchSupport;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent launch point for {@link SnowballItem}
 * (patches/server/PlayerLaunchProjectileEvent.patch).
 *
 * <p>Vanilla 1.21.1 {@code SnowballItem#use} contains exactly one
 * {@code Level#addFreshEntity} and one {@code ItemStack#shrink(1)} call
 * (javap-verified), so the event gate and the {@code shouldConsume()} handling
 * each need a single wrap.
 *
 * <p>Deviation from Paper: Paper moves {@code awardStat} inside the success
 * branch; here the vanilla stat award still runs on a cancelled launch.
 */
@Mixin(SnowballItem.class)
public abstract class SnowballItemMixin {

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean paperarc$launch(Level world, Entity projectile, Operation<Boolean> original,
                                    Level level, Player user, InteractionHand hand) {
        if (world.isClientSide()) {
            return original.call(world, projectile);
        }
        return ProjectileLaunchSupport.callLaunchEvent(user, user.getItemInHand(hand), projectile)
                && original.call(world, projectile);
    }

    @WrapOperation(
            method = "use",
            // 1.21.1 把 shrink(1) 换成了 consume(1, player)（1.20.1 是 shrink）
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void paperarc$consume(ItemStack stack, int amount, net.minecraft.world.entity.LivingEntity holder,
                                  Operation<Void> original,
                                  Level level, Player user, InteractionHand hand) {
        if (ProjectileLaunchSupport.suppressConsume()) {
            if (!LaunchState.isCancelled()) {
                // cancelled launches re-send the inventory from paperarc$result instead
                ProjectileLaunchSupport.updateInventory(user);
            }
            return;
        }
        original.call(stack, amount, holder);
    }

    @ModifyReturnValue(method = "use", at = @At("RETURN"))
    private InteractionResult paperarc$result(InteractionResult original,
                                                               Level level, Player user, InteractionHand hand) {
        if (LaunchState.takeCancelled()) {
            ProjectileLaunchSupport.updateInventory(user);
            return InteractionResult.FAIL;
        }
        return original;
    }
}
