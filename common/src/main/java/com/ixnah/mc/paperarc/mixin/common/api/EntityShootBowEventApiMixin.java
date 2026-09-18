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
 * {@code setConsumeArrow→setConsumeItem}），运行时这三个被转发的方法与
 * {@code consumeItem} 字段都在。
 *
 * <p>消费方是现成的：Arclight 的 {@code BowItemMixin#releaseUsing} 里
 * {@code flag1 = !event.shouldConsumeItem();}，所以 {@code setConsumeArrow(false)}
 * 真的会让箭不被消耗，不是只存个值。
 */
@Mixin(EntityShootBowEvent.class)
public abstract class EntityShootBowEventApiMixin {

    @Shadow
    public abstract ItemStack getConsumable();

    @Shadow
    public abstract boolean shouldConsumeItem();

    @Shadow
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
