package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.World;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Paper 的 {@code ThunderChangeEvent#getCause}；取值来源见 {@code WeatherCauseMixin}。 */
@Mixin(ThunderChangeEvent.class)
public abstract class ThunderChangeEventApiMixin {

    @Unique
    private ThunderChangeEvent.Cause paperarc$cause;

    @Inject(method = "<init>(Lorg/bukkit/World;Z)V", at = @At("RETURN"), remap = false)
    private void paperarc$captureCause(World world, boolean to, CallbackInfo ci) {
        this.paperarc$cause = EventCauseState.takeThunderCause();
    }

    @Unique
    public ThunderChangeEvent.Cause getCause() {
        ThunderChangeEvent.Cause cause = this.paperarc$cause;
        return cause == null ? ThunderChangeEvent.Cause.UNKNOWN : cause;
    }
}
