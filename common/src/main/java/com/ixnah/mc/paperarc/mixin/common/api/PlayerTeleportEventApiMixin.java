package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerTeleportEvent#willDismountPlayer()}（{@code More-Teleport-API.patch}）。
 * {@code PlayerPortalEvent} 继承本类，一并覆盖。
 *
 * <p>值来自 {@code CraftEntityApiMixin#teleport(Location, TeleportCause, TeleportFlag...)}
 * 里真实的下坐骑决定（{@code RETAIN_VEHICLE} 取反），不带 flag 的传送按 CraftBukkit 原行为是
 * {@code true}。
 *
 * <p><b>没补 {@code getRelativeTeleportationFlags()}</b>：Arclight 的传送路径不支持相对传送，
 * 我们的 {@code teleport(..., TeleportFlag...)} 也没有实现 {@code TeleportFlag.Relative}。
 * 返回一个恒为空的集合属于"返回默认值"，按项目原则宁可不补，理由记在 docs/gaps.md §3.1。
 */
@Mixin(PlayerTeleportEvent.class)
public abstract class PlayerTeleportEventApiMixin {

    @Unique
    private boolean paperarc$willDismount = true;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/Location;Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)V",
            at = @At("RETURN"))
    private void paperarc$captureDismount(Player player, Location from, Location to,
                                          PlayerTeleportEvent.TeleportCause cause, CallbackInfo ci) {
        this.paperarc$willDismount = PaperarcEventCauses.teleportDismount();
    }

    @Unique
    public boolean willDismountPlayer() {
        return this.paperarc$willDismount;
    }
}
