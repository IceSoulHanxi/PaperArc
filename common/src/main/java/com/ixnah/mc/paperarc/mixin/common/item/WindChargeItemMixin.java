package com.ixnah.mc.paperarc.mixin.common.item;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's PlayerLaunchProjectileEvent for WindChargeItem.use
 * (PlayerLaunchProjectileEvent.patch).
 *
 * Paper's WindCharge variant has the richest cancel semantics of the launch
 * family: on cancellation it resyncs the container menu, resets the client
 * cooldown display (ClientboundCooldownPacket(this, 0)) and returns FAIL;
 * shouldConsume=false suppresses the consume at the end of use(). The vanilla
 * tail (playSound, addCooldown(10), awardStat, consume) is therefore wrapped
 * individually and suppressed via {@link LaunchState} flags.
 *
 * Bytecode-verified 1.21.1: exactly one call site each for addFreshEntity,
 * playSound, addCooldown, awardStat(Stat) and consume(int, LivingEntity).
 */
@Mixin(WindChargeItem.class)
public abstract class WindChargeItemMixin {

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private boolean paperarc$launch(Level world, Entity projectile, Operation<Boolean> original,
                                    Level level, Player user, InteractionHand hand) {
        if (world.isClientSide) {
            return original.call(world, projectile);
        }
        PlayerLaunchProjectileEvent event = new PlayerLaunchProjectileEvent(
                PaperArcBridge.bukkitPlayer(user),
                CraftItemStack.asCraftMirror(user.getItemInHand(hand)),
                PaperArcBridge.bukkitEntity(projectile));
        if (!event.callEvent()) {
            LaunchState.cancelled(true);
            user.containerMenu.sendAllDataToRemote();
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundCooldownPacket((Item) (Object) this, 0));
            }
            return false;
        }
        LaunchState.noConsume(!event.shouldConsume());
        return original.call(world, projectile);
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V")
    )
    private void paperarc$silence(Level world, Player player, double x, double y, double z,
                                  net.minecraft.sounds.SoundEvent sound, net.minecraft.sounds.SoundSource source,
                                  float volume, float pitch, Operation<Void> original) {
        if (!LaunchState.isCancelled()) {
            original.call(world, player, x, y, z, sound, source, volume, pitch);
        }
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V")
    )
    private void paperarc$noCooldown(ItemCooldowns cooldowns, Item item, int duration, Operation<Void> original) {
        if (!LaunchState.isCancelled()) {
            original.call(cooldowns, item, duration);
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
    private InteractionResultHolder<ItemStack> paperarc$result(InteractionResultHolder<ItemStack> original,
                                                               Level level, Player user, InteractionHand hand) {
        return LaunchState.takeCancelled()
                ? InteractionResultHolder.fail(user.getItemInHand(hand))
                : original;
    }
}
