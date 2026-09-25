package com.ixnah.mc.paperarc.mixin.common.player;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.LaunchState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * PlayerElytraBoostEvent 触发点。
 * <p>
 * 对照 Paper 补丁：FireworkRocketItem#use 鞘翅飞行喷射火箭分支——发事件
 * （mirror ItemStack、待生成 Firework、hand），取消则不生成火箭、不消耗、
 * 不发统计，仅刷新服务端背包；非取消且 spawn 成功后发统计，
 * shouldConsume() 且非创造才 consume(1, player)，否则
 * 刷新服务端背包。语义与 Paper 1.21.11 一致。
 */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemElytraBoostMixin {

    @WrapOperation(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/Projectile;"
            )
    )
    private Projectile paperarc$onElytraBoost(Projectile entity, ServerLevel serverLevel, ItemStack itemStack,
                                              Operation<Projectile> original,
                                              Level methodWorld, Player user, InteractionHand hand) {
        var fireworkRocketEntity = (net.minecraft.world.entity.projectile.FireworkRocketEntity) entity;
        ItemStack held = user.getItemInHand(hand);
        PlayerElytraBoostEvent event = new PlayerElytraBoostEvent(
                PaperArcBridge.bukkitPlayer(user),
                CraftItemStack.asCraftMirror(held),
                (org.bukkit.entity.Firework) PaperArcBridge.bukkitEntity(fireworkRocketEntity),
                CraftEquipmentSlot.getHand(hand));
        if (!event.callEvent()) {
            LaunchState.cancelled(true);
            if (user instanceof ServerPlayer serverPlayer) {
                PaperArcBridge.bukkitPlayer(serverPlayer).updateInventory();
            }
            return entity;
        }
        Projectile spawned = original.call(entity, serverLevel, itemStack);
        if (spawned.isRemoved()) {
            LaunchState.cancelled(true);
            if (user instanceof ServerPlayer serverPlayer) {
                PaperArcBridge.bukkitPlayer(serverPlayer).updateInventory();
            }
        } else {
            LaunchState.noConsume(!event.shouldConsume());
        }
        return spawned;
    }

    @WrapOperation(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void paperarc$noConsume(ItemStack stack, int amount, LivingEntity holder,
                                    Operation<Void> original,
                                    Level level, Player user, InteractionHand hand) {
        if (!LaunchState.isCancelled() && !LaunchState.isNoConsume()) {
            original.call(stack, amount, holder);
        } else if (user instanceof ServerPlayer serverPlayer) {
            PaperArcBridge.bukkitPlayer(serverPlayer).updateInventory();
        }
    }

    @WrapOperation(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V"
            )
    )
    private void paperarc$awardStat(Player player, Stat<?> stat, Operation<Void> original) {
        if (!LaunchState.isCancelled()) {
            original.call(player, stat);
        }
    }

    @ModifyReturnValue(method = "use", at = @At("RETURN"))
    private InteractionResult paperarc$result(InteractionResult original, Level level, Player user, InteractionHand hand) {
        LaunchState.takeCancelled();
        return original;
    }
}
