package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.player.PlayerRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * paper 的 {@code PlayerRespawnEvent#getRespawnFlags()}
 * （{@code add-RespawnFlags-to-PlayerRespawnEvent.patch}）。
 *
 * <p>Paper 是给 {@code PlayerList#respawn} 加一个 varargs 形参把标志一路传进来；
 * 这里不用传 —— 三个标志在**事件自己身上**就已经全了：
 * {@code isBedSpawn()} / {@code isAnchorSpawn()} 是运行时事件已有的字段，
 * {@code END_PORTAL} 等价于 {@code getRespawnReason() == END_PORTAL}
 * （Paper 唯一传 {@code RespawnFlag.END_PORTAL} 的那处，传的同时也传了
 * {@code RespawnReason.END_PORTAL}）。所以直接按现有状态算，不需要触发点配合。
 */
@Mixin(PlayerRespawnEvent.class)
public abstract class PlayerRespawnEventApiMixin {

    @Unique
    public Set<PlayerRespawnEvent.RespawnFlag> getRespawnFlags() {
        PlayerRespawnEvent self = (PlayerRespawnEvent) (Object) this;
        EnumSet<PlayerRespawnEvent.RespawnFlag> flags =
                EnumSet.noneOf(PlayerRespawnEvent.RespawnFlag.class);
        if (self.isBedSpawn()) {
            flags.add(PlayerRespawnEvent.RespawnFlag.BED_SPAWN);
        }
        if (self.isAnchorSpawn()) {
            flags.add(PlayerRespawnEvent.RespawnFlag.ANCHOR_SPAWN);
        }
        if (self.getRespawnReason() == PlayerRespawnEvent.RespawnReason.END_PORTAL) {
            flags.add(PlayerRespawnEvent.RespawnFlag.END_PORTAL);
        }
        return Collections.unmodifiableSet(flags);
    }
}
