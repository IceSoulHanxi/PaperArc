package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 给 {@code PlayerKickEvent#getCause} / {@code PlayerQuitEvent#getReason} /
 * {@code InventoryCloseEvent#getReason} 提供网络层这一侧的取值来源（A7/Y-2 批 2）。
 *
 * <p>{@code tick()} 里有四处 {@code disconnect(Component)}（`javap -c` 核对 srg jar
 * {@code m_9933_}：flying ×2、{@code disconnect.timeout}、
 * {@code multiplayer.disconnect.idling}）。这里**不按 ordinal** 而是按
 * 翻译键判定，Forge/Arclight 挪动顺序也不会认错。
 *
 * <p>{@code disconnect(String)} 是 Arclight 的 {@code ServerPlayNetHandlerMixin}
 * 合并进来的方法（非 {@code @Unique}，保持原名），PlayerKickEvent 就在它里面构造；
 * 我们在 HEAD 只补一个"还没人给出更精确原因时"的默认 QuitReason。
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplKickCauseMixin {

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private void paperarc$tickKickCause(ServerGamePacketListenerImpl self, Component reason,
                                        Operation<Void> original) {
        String key = reason.getContents() instanceof TranslatableContents translatable
                ? translatable.getKey() : "";
        PlayerKickEvent.Cause cause;
        switch (key) {
            case "disconnect.timeout" -> {
                cause = PlayerKickEvent.Cause.TIMEOUT;
                EventCauseState.setQuitReason(PlayerQuitEvent.QuitReason.TIMED_OUT);
            }
            case "multiplayer.disconnect.idling" -> cause = PlayerKickEvent.Cause.IDLING;
            case "multiplayer.disconnect.flying" -> cause = PlayerKickEvent.Cause.FLYING_PLAYER;
            default -> cause = PlayerKickEvent.Cause.UNKNOWN;
        }
        EventCauseState.setKickCause(cause);
        try {
            original.call(self, reason);
        } finally {
            EventCauseState.clearKickCause();
            EventCauseState.clearQuitReason();
        }
    }

    @Inject(method = "disconnect(Ljava/lang/String;)V", at = @At("HEAD"), remap = false)
    private void paperarc$defaultQuitReason(String reason, CallbackInfo ci) {
        EventCauseState.setQuitReasonIfAbsent(PlayerQuitEvent.QuitReason.KICKED);
    }

    @WrapOperation(
            method = "handleContainerClose",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;doCloseContainer()V"))
    private void paperarc$playerClosedInventory(ServerPlayer player, Operation<Void> original) {
        EventCauseState.setInventoryCloseReason(InventoryCloseEvent.Reason.PLAYER);
        try {
            original.call(player);
        } finally {
            EventCauseState.clearInventoryCloseReason();
        }
    }
}
