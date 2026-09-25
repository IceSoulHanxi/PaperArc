package com.ixnah.mc.paperarc.mixin.common.item;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent for WindChargeItem.use
 * (PlayerLaunchProjectileEvent.patch).
 */
@Mixin(WindChargeItem.class)
public abstract class WindChargeItemMixin {

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
        PlayerLaunchProjectileEvent event = new PlayerLaunchProjectileEvent(
                PaperArcBridge.bukkitPlayer(user),
                CraftItemStack.asCraftMirror(user.getItemInHand(hand)),
                PaperArcBridge.bukkitEntity(projectile));
        if (!event.callEvent()) {
            LaunchState.cancelled(true);
            user.containerMenu.sendAllDataToRemote();
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundCooldownPacket(user.getCooldowns().getCooldownGroup(user.getItemInHand(hand)), 0));
            }
            return projectile;
        }
        LaunchState.noConsume(!event.shouldConsume());
        return Projectile.spawnProjectile(projectile, serverLevel, stack,
                p -> p.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), z, velocity, inaccuracy));
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V")
    )
    private void paperarc$silence(Level world, net.minecraft.world.entity.Entity player, double x, double y, double z,
                                  net.minecraft.sounds.SoundEvent sound, net.minecraft.sounds.SoundSource source,
                                  float volume, float pitch, Operation<Void> original) {
        if (!LaunchState.isCancelled()) {
            original.call(world, player, x, y, z, sound, source, volume, pitch);
        }
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V")
    )
    private void paperarc$noStat(Player user, Stat<?> stat, Operation<Void> original) {
        if (!LaunchState.isCancelled()) {
            original.call(user, stat);
        }
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void paperarc$noConsume(ItemStack stack, int amount, LivingEntity entity, Operation<Void> original) {
        if (!LaunchState.isCancelled() && !LaunchState.isNoConsume()) {
            original.call(stack, amount, entity);
        }
    }

    @ModifyReturnValue(method = "use", at = @At("RETURN"))
    private InteractionResult paperarc$result(InteractionResult original,
                                              Level level, Player user, InteractionHand hand) {
        return LaunchState.takeCancelled()
                ? InteractionResult.FAIL
                : original;
    }
}

