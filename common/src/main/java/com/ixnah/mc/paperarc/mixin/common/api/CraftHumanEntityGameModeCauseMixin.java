package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 插件调 {@code HumanEntity#setGameMode} 的 {@code PlayerGameModeChangeEvent.Cause.PLUGIN}。 */
@Mixin(CraftHumanEntity.class)
public abstract class CraftHumanEntityGameModeCauseMixin {

    @Inject(method = "setGameMode", at = @At("HEAD"), remap = false)
    private void paperarc$pushPluginCause(GameMode mode, CallbackInfo ci) {
        PaperarcEventCauses.pushGameMode(PlayerGameModeChangeEvent.Cause.PLUGIN);
    }

    @Inject(method = "setGameMode", at = @At("RETURN"), remap = false)
    private void paperarc$popPluginCause(GameMode mode, CallbackInfo ci) {
        PaperarcEventCauses.popGameMode();
    }
}
