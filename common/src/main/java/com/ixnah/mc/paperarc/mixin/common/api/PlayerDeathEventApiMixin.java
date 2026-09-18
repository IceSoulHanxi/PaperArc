package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code PlayerDeathEvent} 上的 {@code getPlayer()} 与 adventure 版
 * {@code deathMessage()}（checklist §1.10 am，第 ④ 批）。
 *
 * <p>B7/Y-2 批 1 补上 {@code getItemsToKeep()} 与 {@code shouldDropExperience()} 一对，
 * 两者都有真正的消费方：{@code bridge/api/PaperarcDeathEvents#afterFired} 在事件派发之后
 * 把不掉的物品从掉落列表里摘出来（重生时由 {@code player.ServerPlayerKeptItemsMixin} 还回去）、
 * 把 {@code shouldDropExperience()==false} 落成 {@code setDroppedExp(0)}。</p>
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
        this.paperarc$self().setDeathMessage(
                deathMessage == null ? null : LegacyComponentSerializer.legacySection().serialize(deathMessage));
    }

    @Unique
    private final java.util.List<org.bukkit.inventory.ItemStack> paperarc$itemsToKeep = new java.util.ArrayList<>();

    @Unique
    private boolean paperarc$shouldDropExperience = true;

    @Unique
    public java.util.List<org.bukkit.inventory.ItemStack> getItemsToKeep() {
        return this.paperarc$itemsToKeep;
    }

    @Unique
    public boolean shouldDropExperience() {
        return this.paperarc$shouldDropExperience;
    }

    @Unique
    public void setShouldDropExperience(boolean dropExperience) {
        this.paperarc$shouldDropExperience = dropExperience;
    }
}
