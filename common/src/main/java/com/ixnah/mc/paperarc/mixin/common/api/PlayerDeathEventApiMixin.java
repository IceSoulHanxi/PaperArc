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
 * <p>没补 {@code getItemsToKeep()} 与 {@code shouldDropExperience()} 一对：
 * 它们要 NMS 侧的掉落流程真的读这份状态才有意义，只加字段等于静默失效，
 * 见 docs/gaps.md。</p>
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
}
