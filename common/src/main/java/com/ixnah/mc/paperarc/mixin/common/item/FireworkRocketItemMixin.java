package com.ixnah.mc.paperarc.mixin.common.item;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent for FireworkRocketItem.useOn
 * (PlayerLaunchProjectileEvent.patch — Paper fires the event in useOn,
 * i.e. right-clicking a block with a rocket; useOn cancel returns PASS).
 *
 * In 1.21.11 useOn: Projectile.spawnProjectile(...) then itemStack.shrink(1)
 * then SUCCESS. Paper gates shrink with shouldConsume && !hasInfiniteMaterials
 * and otherwise resyncs inventory.
 */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {

    @WrapOperation(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/Projectile;"
            )
    )
    private Projectile paperarc$launch(Projectile projectile, ServerLevel serverLevel, ItemStack itemStack,
                                       Operation<Projectile> original, UseOnContext context) {
        if (context.getPlayer() == null) {
            return original.call(projectile, serverLevel, itemStack);
        }
        PlayerLaunchProjectileEvent event = new PlayerLaunchProjectileEvent(
                PaperArcBridge.bukkitPlayer(context.getPlayer()),
                CraftItemStack.asCraftMirror(itemStack),
                PaperArcBridge.bukkitEntity(projectile));
        if (!event.callEvent()) {
            LaunchState.cancelled(true);
            return projectile;
        }
        Projectile spawned = original.call(projectile, serverLevel, itemStack);
        if (spawned.isRemoved()) {
            LaunchState.cancelled(true);
        } else {
            LaunchState.noConsume(!event.shouldConsume());
        }
        return spawned;
    }

    @WrapOperation(
            method = "useOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    )
    private void paperarc$noShrink(ItemStack stack, int amount, Operation<Void> original, UseOnContext context) {
        if (!LaunchState.isCancelled() && !LaunchState.isNoConsume()) {
            original.call(stack, amount);
        }
    }

    @ModifyReturnValue(method = "useOn", at = @At("RETURN"))
    private InteractionResult paperarc$result(InteractionResult original, UseOnContext context) {
        return LaunchState.takeCancelled() ? InteractionResult.PASS : original;
    }
}
