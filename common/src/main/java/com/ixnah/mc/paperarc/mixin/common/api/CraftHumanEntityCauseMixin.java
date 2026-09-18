package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 插件侧入口给 {@code InventoryCloseEvent#getReason} / {@code PlayerGameModeChangeEvent#getCause}
 * 提供 {@code PLUGIN}。
 *
 * <p>{@code closeInventory()} 用 "if absent"：调用链外层可能已经给出更精确的原因
 * （下线是 DISCONNECT、死亡是 DEATH，见 {@code PlayerListRemoveReasonMixin} /
 * {@code ServerPlayerDeathCloseReasonMixin}），不要把它们盖掉。
 */
@Mixin(value = CraftHumanEntity.class, remap = false)
public abstract class CraftHumanEntityCauseMixin {

    @Inject(method = "closeInventory()V", at = @At("HEAD"), remap = false)
    private void paperarc$pluginCloseReason(CallbackInfo ci) {
        EventCauseState.setInventoryCloseReasonIfAbsent(InventoryCloseEvent.Reason.PLUGIN);
    }

    @Inject(method = "setGameMode(Lorg/bukkit/GameMode;)V", at = @At("HEAD"), remap = false)
    private void paperarc$pluginGameModeCause(GameMode mode, CallbackInfo ci) {
        EventCauseState.setGameModeCause(PlayerGameModeChangeEvent.Cause.PLUGIN);
    }

    @Inject(method = "setGameMode(Lorg/bukkit/GameMode;)V", at = @At("RETURN"), remap = false)
    private void paperarc$clearPluginGameModeCause(GameMode mode, CallbackInfo ci) {
        EventCauseState.clearGameModeCause();
    }
}
