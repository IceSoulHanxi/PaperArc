package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

/**
 * paper 让 {@code org.bukkit.inventory.ItemStack} 实现
 * {@code net.kyori.adventure.text.event.HoverEventSource<HoverEvent.ShowItem>} 与
 * {@code io.papermc.paper.persistence.PersistentDataViewHolder}
 *（gaps.md §0「implements 缺口」那一行）。不实现的话，所有形参是这两个接口的 API
 * （adventure 的 {@code Component#hoverEvent(HoverEventSource)}、paper 的
 * {@code PersistentDataViewHolder}）插件传 ItemStack 进去就是 {@code ClassCastException}。
 *
 * <p><b>本类只加父接口，一个方法都不写</b>：两个抽象方法
 * （{@code asHoverEvent(UnaryOperator)}、{@code getPersistentDataContainer()}）的实现体
 * 早就在 {@code api.ItemStackApiMixin} 上了。第一版在这里又写了一遍 {@code asHoverEvent}，
 * 真机日志立刻是
 * {@code Discarding @Unique public method asHoverEvent … because it already exists}
 * （两个 mixin 给同一目标加同名方法，后一个被丢；门禁判 FAIL）。
 */
@Mixin(ItemStack.class)
public abstract class ItemStackAdventureMixin
        implements HoverEventSource<HoverEvent.ShowItem>, io.papermc.paper.persistence.PersistentDataViewHolder {
}
