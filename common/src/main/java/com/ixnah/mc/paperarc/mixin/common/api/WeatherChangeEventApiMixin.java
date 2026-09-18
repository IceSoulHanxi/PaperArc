package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.World;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Paper 的 {@code WeatherChangeEvent#getCause}；取值来源见 {@code WeatherCauseMixin}。 */
@Mixin(WeatherChangeEvent.class)
public abstract class WeatherChangeEventApiMixin {

    @Unique
    private WeatherChangeEvent.Cause paperarc$cause;

    @Inject(method = "<init>(Lorg/bukkit/World;Z)V", at = @At("RETURN"), remap = false)
    private void paperarc$captureCause(World world, boolean to, CallbackInfo ci) {
        this.paperarc$cause = EventCauseState.takeWeatherCause();
    }

    @Unique
    public WeatherChangeEvent.Cause getCause() {
        WeatherChangeEvent.Cause cause = this.paperarc$cause;
        return cause == null ? WeatherChangeEvent.Cause.UNKNOWN : cause;
    }
}
