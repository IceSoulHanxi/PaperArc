package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * paper 的 {@code PlayerDeathEvent} Component 版死亡消息与协变的 {@code getPlayer()}
 * （A6/X-2 第六批）。两者都只是对事件已有状态换个类型，没有新状态。
 *
 * <p>A7/Y-2 批 1 补上 {@code getItemsToKeep()}/{@code shouldDropExperience()}：
 * 两个字段都不写初始化表达式（{@code itemsToKeep} 懒建、掉经验用取反的
 * {@code noDropExperience}），不依赖"Mixin 把字段初始化器并进目标构造器"这条行为。
 * 消费方在 {@code ServerPlayerDeathEventMixin}（{@code die} 里的 {@code dropExperience()}
 * 与 {@code Inventory.clearContent()} 两个调用点）。取消语义继承自
 * {@code EntityDeathEventApiMixin}。
 */
@Mixin(PlayerDeathEvent.class)
public abstract class PlayerDeathEventApiMixin {

    @Unique
    private PlayerDeathEvent paperarc$self() {
        return (PlayerDeathEvent) (Object) this;
    }

    @Unique
    public Player getPlayer() {
        return (Player) this.paperarc$self().getEntity();
    }

    @Unique
    public Component deathMessage() {
        String legacy = this.paperarc$self().getDeathMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void deathMessage(Component deathMessage) {
        this.paperarc$self().setDeathMessage(deathMessage == null ? null
                : LegacyComponentSerializer.legacySection().serialize(deathMessage));
    }

    @Unique
    private List<ItemStack> paperarc$itemsToKeep;

    @Unique
    private boolean paperarc$noDropExperience;

    @Unique
    public List<ItemStack> getItemsToKeep() {
        List<ItemStack> items = this.paperarc$itemsToKeep;
        if (items == null) {
            items = new ArrayList<>();
            this.paperarc$itemsToKeep = items;
        }
        return items;
    }

    @Unique
    public boolean shouldDropExperience() {
        return !this.paperarc$noDropExperience;
    }

    @Unique
    public void setShouldDropExperience(boolean dropExperience) {
        this.paperarc$noDropExperience = !dropExperience;
    }
}
