package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code EntityShootBowEvent#getArrowItem()/getConsumeArrow()/setConsumeArrow(boolean)}。
 *
 * <p>三条都是 paper 给旧 API 留的**同义转发**（`javap -c` 核对 paper-api：
 * {@code getArrowItem→getConsumable}、{@code getConsumeArrow→shouldConsumeItem}、
 * {@code setConsumeArrow→setConsumeItem}），被转发的三个方法运行时都在。
 *
 * <p>消费方 <b>1.21.1 没有现成的</b>：vanilla 把箭的消耗挪到了
 * {@code BowItem#releaseUsing → ProjectileWeaponItem.draw → useAmmo}，而事件是之后在
 * {@code shoot} 里才 fire 的，Arclight 也不读 {@code shouldConsumeItem()}
 * （`javap` 核对 {@code ProjectileWeaponItemMixin#arclight$shootBow} 与
 * {@code AbstractSkeletonMixin}，全仓只有事件类自己提到这个方法）。
 * 所以由 {@code api.CraftEventFactoryShootBowMixin} 在事件派发之后**把箭还回去**补上这一步。
 */
@Mixin(EntityShootBowEvent.class)
public abstract class EntityShootBowEventApiMixin {

    @Shadow(remap = false)
    public abstract ItemStack getConsumable();

    @Shadow(remap = false)
    public abstract boolean shouldConsumeItem();

    @Shadow(remap = false)
    public abstract void setConsumeItem(boolean consumeItem);

    @Unique
    public ItemStack getArrowItem() {
        return this.getConsumable();
    }

    @Unique
    public boolean getConsumeArrow() {
        return this.shouldConsumeItem();
    }

    @Unique
    public void setConsumeArrow(boolean consumeItem) {
        this.setConsumeItem(consumeItem);
    }
}
