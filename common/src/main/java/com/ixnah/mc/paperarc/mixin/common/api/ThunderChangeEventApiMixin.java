package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.World;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code ThunderChangeEvent#getCause()}，与 {@link WeatherChangeEventApiMixin} 同构。
 */
@Mixin(ThunderChangeEvent.class)
public abstract class ThunderChangeEventApiMixin {

    @Unique
    private ThunderChangeEvent.Cause paperarc$cause = ThunderChangeEvent.Cause.UNKNOWN;

    @Inject(method = "<init>(Lorg/bukkit/World;Z)V", at = @At("RETURN"))
    private void paperarc$captureCause(World world, boolean to, CallbackInfo ci) {
        this.paperarc$cause = PaperarcEventCauses.thunder();
    }

    @Unique
    public ThunderChangeEvent.Cause getCause() {
        return this.paperarc$cause;
    }
}
