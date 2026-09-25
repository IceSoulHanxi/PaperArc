package com.ixnah.mc.paperarc.mixin.common.item;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent launch point for tridents
 * (patches/server/PlayerLaunchProjectileEvent.patch -> TridentItem#releaseUsing).
 */
@Mixin(TridentItem.class)
public abstract class TridentItemMixin {

    @WrapOperation(
            method = "releaseUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"
            )
    )
    private <T extends Projectile> T paperarc$launch(Projectile.ProjectileFactory<T> factory, ServerLevel serverLevel,
                                                     ItemStack itemStack, LivingEntity user, float z, float velocity, float inaccuracy,
                                                     Operation<T> original,
                                                     ItemStack stack, Level level, LivingEntity methodUser, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return original.call(factory, serverLevel, itemStack, user, z, velocity, inaccuracy);
        }
        T projectile = factory.create(serverLevel, user, itemStack);
        PlayerLaunchProjectileEvent event = new PlayerLaunchProjectileEvent(
                PaperArcBridge.bukkitPlayer(player),
                CraftItemStack.asCraftMirror(stack),
                PaperArcBridge.bukkitEntity(projectile));
        if (!event.callEvent()) {
            LaunchState.cancelled(true);
            return projectile;
        }
        T spawned = original.call(factory, serverLevel, itemStack, user, z, velocity, inaccuracy);
        if (spawned.isRemoved()) {
            LaunchState.cancelled(true);
        } else {
            LaunchState.noConsume(!event.shouldConsume());
        }
        return spawned;
    }

    @WrapOperation(
            method = "releaseUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
            )
    )
    private void paperarc$playSound(Level instance, Entity source, Entity target, SoundEvent sound, SoundSource source2,
                                    float volume, float pitch, Operation<Void> original) {
        if (!LaunchState.isCancelled()) {
            original.call(instance, source, target, sound, source2, volume, pitch);
        }
    }

    @ModifyReturnValue(method = "releaseUsing", at = @At("RETURN"))
    private boolean paperarc$result(boolean original, ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        boolean cancelled = LaunchState.isCancelled();
        boolean noConsume = LaunchState.isNoConsume();
        LaunchState.takeCancelled();
        if (cancelled) {
            if (user instanceof Player player && !player.hasInfiniteMaterials()) {
                stack.grow(1);
            }
            if (user instanceof ServerPlayer serverPlayer) {
                PaperArcBridge.bukkitPlayer(serverPlayer).updateInventory();
            }
            return false;
        }
        if (noConsume) {
            if (user instanceof Player player && !player.hasInfiniteMaterials()) {
                stack.grow(1);
            }
            if (user instanceof ServerPlayer serverPlayer) {
                PaperArcBridge.bukkitPlayer(serverPlayer).updateInventory();
            }
        }
        return original;
    }
}
