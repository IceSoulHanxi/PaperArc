package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code PlayerDeathEvent} Component 版死亡消息与协变的 {@code getPlayer()}
 * （A6/X-2 第六批）。两者都只是对事件已有状态换个类型，没有新状态。
 *
 * <p>{@code getItemsToKeep()}/{@code shouldDropExperience()} 需要死亡处理那一侧配合
 * （keepInventory 的物品清单在 NMS 侧），不做假实现，见 docs/gaps.md。
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
}
