package com.ixnah.mc.paperarc.mixin.common.item;

import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.ProjectileLaunchSupport;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent launch point for {@link EggItem}
 * (patches/server/PlayerLaunchProjectileEvent.patch).
 *
 * <p>In 1.21.11, {@code EggItem#use} delegates projectile spawning to
 * {@code Projectile#spawnProjectileFromRotation}, followed by {@code ItemStack#consume(1, player)}.
 * We wrap {@code spawnProjectileFromRotation} to fire the event and control spawning,
 * and wrap {@code consume} to honor {@code shouldConsume()}.
 */
@Mixin(EggItem.class)
public abstract class EggItemMixin {

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    private <T extends Projectile> T paperarc$launch(
            Projectile.ProjectileFactory<T> factory,
            ServerLevel serverLevel,
            ItemStack stack,
            LivingEntity shooter,
            float z,
            float velocity,
            float inaccuracy,
            Operation<T> original,
            Level level,
            Player user,
            InteractionHand hand) {
        T projectile = factory.create(serverLevel, shooter, stack);
        if (!ProjectileLaunchSupport.callLaunchEvent(user, user.getItemInHand(hand), projectile)) {
            return projectile;
        }
        return Projectile.spawnProjectile(projectile, serverLevel, stack,
                p -> p.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), z, velocity, inaccuracy));
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
