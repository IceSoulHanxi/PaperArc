package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code Weather/ThunderChangeEvent#getCause} 的 {@code COMMAND} 来源：
 * {@code /weather} 的三个子命令都只调一次
 * {@code ServerLevel#setWeatherParameters(int,int,boolean,boolean)}。
 */
@Mixin(WeatherCommand.class)
public abstract class WeatherCommandCauseMixin {

    @WrapOperation(
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setWeatherParameters(IIZZ)V"),
            method = {"setClear", "setRain", "setThunder"})
    private static void paperarc$commandWeatherCause(ServerLevel level, int clearTime, int weatherTime,
                                                     boolean raining, boolean thundering,
                                                     Operation<Void> original) {
        EventCauseState.setWeatherCause(WeatherChangeEvent.Cause.COMMAND);
        EventCauseState.setThunderCause(ThunderChangeEvent.Cause.COMMAND);
        try {
            original.call(level, clearTime, weatherTime, raining, thundering);
        } finally {
            EventCauseState.clearWeatherCause();
            EventCauseState.clearThunderCause();
        }
    }
}
