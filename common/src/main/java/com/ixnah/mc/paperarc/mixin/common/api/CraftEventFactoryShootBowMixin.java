package com.ixnah.mc.paperarc.mixin.common.api;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code EntityShootBowEvent#setConsumeArrow(false)}（= spigot 的 {@code setConsumeItem}）
 * 的消费方。
 *
 * <p>1.21.1 的 vanilla 把箭的扣除挪到了 {@code BowItem#releaseUsing} 里的
 * {@code ProjectileWeaponItem.draw → useAmmo}，**早于**事件派发所在的
 * {@code ProjectileWeaponItem#shoot}；Arclight 那边也没有任何地方读
 * {@code shouldConsumeItem()}（全仓 javap 核对）。所以"不消耗"没法靠"别扣"实现，
 * 只能在事件派发之后**把这一份还回玩家背包** —— 结果等价。
 *
 * <p>三个前提：事件没被取消（取消时 Arclight 自己会收拾）、射手是玩家、
 * 这一份不是无限箭/创造模式发的虚拟箭（vanilla 给那种箭打了
 * {@code INTANGIBLE_PROJECTILE} 组件，本来就没从背包里扣）。
 */
@Mixin(CraftEventFactory.class)
public abstract class CraftEventFactoryShootBowMixin {

    @ModifyReturnValue(method = "callEntityShootBowEvent", at = @At("RETURN"), remap = false)
    private static EntityShootBowEvent paperarc$refundArrow(EntityShootBowEvent event,
                                                            LivingEntity shooter, ItemStack bow,
                                                            ItemStack consumable, Entity projectile,
                                                            InteractionHand hand, float force,
                                                            boolean consumeItem) {
        if (event.isCancelled() || event.shouldConsumeItem() || !consumeItem) {
            return event;
        }
        if (!(shooter instanceof Player player) || consumable == null || consumable.isEmpty()) {
            return event;
        }
        if (consumable.has(DataComponents.INTANGIBLE_PROJECTILE)) {
            return event;
        }
        ItemStack refund = consumable.copy();
        if (!player.getInventory().add(refund)) {
            player.drop(refund, false);
        }
        return event;
    }
}
