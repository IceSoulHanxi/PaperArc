package dev.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityDamageItemEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

/**
 * Port of Paper's EntityDamageItemEvent for the enchantment path.
 * Paper changes {@code DamageItem.apply} to pass {@code context.owner()}
 * through instead of null for non-players. We wrap the hurtAndBreak call
 * (receiver = the stack) and pull the enchantment context in via an
 * {@code @Local} sugar; when the owner is a non-player living entity we fire
 * EntityDamageItemEvent: cancelled drops the damage entirely, otherwise the
 * (possibly modified) amount is applied.
 */
@Mixin(net.minecraft.world.item.enchantment.effects.DamageItem.class)
public abstract class DamageItemMixin {

    @WrapOperation(
            method = "apply",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V")
    )
    private void paperarc$damageItem(ItemStack instance, int amount, ServerLevel level,
                                     ServerPlayer player, Consumer<Item> onBreak,
                                     Operation<Void> original,
                                     @Local EnchantedItemInUse context) {
        LivingEntity owner = context.owner();
        if (owner == null || owner instanceof ServerPlayer) {
            original.call(instance, amount, level, player, onBreak);
            return;
        }
        EntityDamageItemEvent event = new EntityDamageItemEvent(
                PaperArcBridge.bukkitEntity(owner), CraftItemStack.asCraftMirror(instance), amount);
        if (event.callEvent()) {
            original.call(instance, event.getDamage(), level, player, onBreak);
        }
    }
}
