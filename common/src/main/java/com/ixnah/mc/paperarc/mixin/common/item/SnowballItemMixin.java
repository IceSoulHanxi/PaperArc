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
 * <p>In Arclight GoS 1.21.11, {@code SnowballItem#use} is overwritten by Arclight's
 * {@code SnowballItemMixin} to construct a {@code Snowball} entity directly and invoke
 * {@code Level#addFreshEntity(Entity)}, followed by {@code ItemStack#consume(1, player)}.
 * We wrap {@code Level#addFreshEntity} to fire the event and control spawning,
 * and wrap {@code consume} to honor {@code shouldConsume()}.
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
