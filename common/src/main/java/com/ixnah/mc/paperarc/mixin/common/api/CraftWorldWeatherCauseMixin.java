package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code Weather/ThunderChangeEvent#getCause} 的 {@code PLUGIN} 来源：
 * 插件调 {@code World#setStorm/setThundering}。
 */
@Mixin(value = CraftWorld.class, remap = false)
public abstract class CraftWorldWeatherCauseMixin {

    @Inject(method = "setStorm(Z)V", at = @At("HEAD"), remap = false)
    private void paperarc$pluginWeatherCause(boolean hasStorm, CallbackInfo ci) {
        EventCauseState.setWeatherCause(WeatherChangeEvent.Cause.PLUGIN);
    }

    @Inject(method = "setStorm(Z)V", at = @At("RETURN"), remap = false)
    private void paperarc$clearPluginWeatherCause(boolean hasStorm, CallbackInfo ci) {
        EventCauseState.clearWeatherCause();
    }

    @Inject(method = "setThundering(Z)V", at = @At("HEAD"), remap = false)
    private void paperarc$pluginThunderCause(boolean thundering, CallbackInfo ci) {
        EventCauseState.setThunderCause(ThunderChangeEvent.Cause.PLUGIN);
    }

    @Inject(method = "setThundering(Z)V", at = @At("RETURN"), remap = false)
    private void paperarc$clearPluginThunderCause(boolean thundering, CallbackInfo ci) {
        EventCauseState.clearThunderCause();
    }
}
