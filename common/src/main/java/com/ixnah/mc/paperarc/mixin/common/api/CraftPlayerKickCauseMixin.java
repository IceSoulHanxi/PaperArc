package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.event.player.PlayerKickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 插件调 {@code Player#kickPlayer(String)} 的 {@code PlayerKickEvent.Cause.PLUGIN}。 */
@Mixin(CraftPlayer.class)
public abstract class CraftPlayerKickCauseMixin {

    @Inject(method = "kickPlayer", at = @At("HEAD"), remap = false)
    private void paperarc$pushPluginCause(String message, CallbackInfo ci) {
        PaperarcEventCauses.pushKick(PlayerKickEvent.Cause.PLUGIN);
    }

    @Inject(method = "kickPlayer", at = @At("RETURN"), remap = false)
    private void paperarc$popPluginCause(String message, CallbackInfo ci) {
        PaperarcEventCauses.popKick();
    }
}
