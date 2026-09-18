package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 adventure 版 {@code joinMessage()}（checklist §1.10 am，第 ④ 批）。
 * 运行时事件是 String 存储的，这里用 legacy-section 序列化器双向桥接 —— 与 B2 在
 * ItemMeta 的 displayName/lore 上用的是同一条路子，取值可往返。
 */
@Mixin(PlayerJoinEvent.class)
public abstract class PlayerJoinEventApiMixin {

    @Unique
    public Component joinMessage() {
        String legacy = ((PlayerJoinEvent) (Object) this).getJoinMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void joinMessage(Component joinMessage) {
        ((PlayerJoinEvent) (Object) this).setJoinMessage(
                joinMessage == null ? null : LegacyComponentSerializer.legacySection().serialize(joinMessage));
    }
}
