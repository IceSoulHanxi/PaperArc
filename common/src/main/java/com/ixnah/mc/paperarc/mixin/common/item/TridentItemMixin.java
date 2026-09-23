package com.ixnah.mc.paperarc.mixin.common.item;

import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.ProjectileLaunchSupport;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's PlayerLaunchProjectileEvent launch point for tridents
 * (patches/server/PlayerLaunchProjectileEvent.patch -> TridentItem#releaseUsing).
 *
 * <p>Vanilla 1.20.1 {@code releaseUsing} has exactly one
 * {@code Level#addFreshEntity} and one {@code Inventory#removeItem}
 * (javap-verified); the throw sound is the first of the two
 * {@code Level#playSound(Player,Entity,SoundEvent,SoundSource,FF)} calls.
 *
 * <p><b>Deviation from Paper</b>: Paper moves {@code stack.hurtAndBreak(1, …)}
 * below the event so a cancelled throw costs no durability. That call sits
 * <i>before</i> the trident entity exists, so the event cannot be fired early
 * enough to gate it with an injector; a cancelled throw therefore still consumes
 * one point of durability, and {@code awardStat} still runs. Recorded in
 * docs/gaps.md.
 */
@Mixin(TridentItem.class)
public abstract class TridentItemMixin {

    @WrapOperation(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean paperarc$launch(Level world, Entity trident, Operation<Boolean> original,
                                    ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (world.isClientSide() || !(user instanceof Player)) {
            return original.call(world, trident);
        }
        return ProjectileLaunchSupport.callLaunchEvent((Player) user, stack, trident)
                && original.call(world, trident);
    }

    @WrapOperation(
            method = "releaseUsing",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V")
    )
    private void paperarc$throwSound(Level world, Player near, Entity source, SoundEvent sound, SoundSource category,
                                     float volume, float pitch, Operation<Void> original,
                                     ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (LaunchState.isCancelled()) {
            return;
        }
        original.call(world, near, source, sound, category, volume, pitch);
    }

    @WrapOperation(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;removeItem(Lnet/minecraft/world/item/ItemStack;)V")
    )
    private void paperarc$consume(Inventory inventory, ItemStack stack, Operation<Void> original,
                                  ItemStack usedStack, Level level, LivingEntity user, int remainingUseTicks) {
        if (ProjectileLaunchSupport.suppressConsume()) {
            if (user instanceof Player) {
                ProjectileLaunchSupport.updateInventory((Player) user);
            }
            return;
        }
        original.call(inventory, stack);
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void paperarc$clearLaunchState(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks,
                                           CallbackInfo ci) {
        LaunchState.takeCancelled();
    }
}
