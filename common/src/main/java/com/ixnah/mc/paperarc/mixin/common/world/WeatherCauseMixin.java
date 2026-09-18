package com.ixnah.mc.paperarc.mixin.common.world;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code Weather/ThunderChangeEvent#getCause} 的 {@code SLEEP} 来源：
 * 全服睡觉跳过夜晚时 vanilla 调 {@code ServerLevel#resetWeatherCycle()}
 * （1.20.1 里只有 {@code ServerLevel#tick} 的"所有人都睡了"分支会调它）。
 * 其余情况的默认值是 {@code NATURAL}，与 Paper 一致。
 */
@Mixin(ServerLevel.class)
public abstract class WeatherCauseMixin {

    @Inject(method = "resetWeatherCycle", at = @At("HEAD"))
    private void paperarc$sleepWeatherCause(CallbackInfo ci) {
        EventCauseState.setWeatherCause(WeatherChangeEvent.Cause.SLEEP);
        EventCauseState.setThunderCause(ThunderChangeEvent.Cause.SLEEP);
    }

    @Inject(method = "resetWeatherCycle", at = @At("RETURN"))
    private void paperarc$clearSleepWeatherCause(CallbackInfo ci) {
        EventCauseState.clearWeatherCause();
        EventCauseState.clearThunderCause();
    }
}
