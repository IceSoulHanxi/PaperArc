package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Paper 的 {@code PlayerRespawnEvent#getRespawnFlags}。
 *
 * <p>三个 flag 都能从事件自身已有的状态推出来，不需要在触发点另传：
 * {@code BED_SPAWN} = 运行时构造器的 {@code isBedSpawn}、
 * {@code ANCHOR_SPAWN} = {@code isAnchorSpawn}、
 * {@code END_PORTAL} = {@code getRespawnReason() == END_PORTAL}
 * （Arclight 在 {@code PlayerList#respawn} 里传的就是这三样，
 * `javap`/源码核对 {@code new PlayerRespawnEvent(p, loc, isBedSpawn && !flag3, flag3, reason)}）。
 */
@Mixin(PlayerRespawnEvent.class)
public abstract class PlayerRespawnEventApiMixin {

    @Unique
    private Set<PlayerRespawnEvent.RespawnFlag> paperarc$respawnFlags;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/Location;ZZLorg/bukkit/event/player/PlayerRespawnEvent$RespawnReason;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureFlags(Player player, Location respawnLocation, boolean isBedSpawn,
                                       boolean isAnchorSpawn, PlayerRespawnEvent.RespawnReason respawnReason,
                                       CallbackInfo ci) {
        EnumSet<PlayerRespawnEvent.RespawnFlag> flags =
                EnumSet.noneOf(PlayerRespawnEvent.RespawnFlag.class);
        if (isBedSpawn) {
            flags.add(PlayerRespawnEvent.RespawnFlag.BED_SPAWN);
        }
        if (isAnchorSpawn) {
            flags.add(PlayerRespawnEvent.RespawnFlag.ANCHOR_SPAWN);
        }
        if (respawnReason == PlayerRespawnEvent.RespawnReason.END_PORTAL) {
            flags.add(PlayerRespawnEvent.RespawnFlag.END_PORTAL);
        }
        this.paperarc$respawnFlags = Collections.unmodifiableSet(flags);
    }

    @Unique
    public Set<PlayerRespawnEvent.RespawnFlag> getRespawnFlags() {
        Set<PlayerRespawnEvent.RespawnFlag> flags = this.paperarc$respawnFlags;
        return flags == null ? Collections.emptySet() : flags;
    }
}
