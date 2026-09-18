package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.World;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code WeatherChangeEvent#getCause()}（gaps.md §3.1 Cause/Reason 一组）。
 * 原因由上游 vanilla 调用点压进 {@link PaperarcEventCauses}，这里在构造器里取回。
 * 触发点见 {@code world.ServerLevelWeatherCauseMixin} 与 {@code api.CraftWorldWeatherCauseMixin}。
 */
@Mixin(WeatherChangeEvent.class)
public abstract class WeatherChangeEventApiMixin {

    @Unique
    private WeatherChangeEvent.Cause paperarc$cause = WeatherChangeEvent.Cause.UNKNOWN;

    @Inject(method = "<init>(Lorg/bukkit/World;Z)V", at = @At("RETURN"))
    private void paperarc$captureCause(World world, boolean to, CallbackInfo ci) {
        this.paperarc$cause = PaperarcEventCauses.weather();
    }

    @Unique
    public WeatherChangeEvent.Cause getCause() {
        return this.paperarc$cause;
    }
}
