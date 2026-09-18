package com.ixnah.mc.paperarc.mixin.common.world;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's {@code Add-cause-to-Weather-ThunderChangeEvents.patch}（服务端一侧）。
 *
 * <p>Paper 的做法是给 {@code ServerLevelData#setRaining/setThundering} 加一个 Cause 形参，
 * 三个调用点各传各的。我们加不了形参（那是运行时已有的接口方法），改成在每个调用点用
 * {@code @WrapOperation} 把原因压进 ThreadLocal 再放掉 —— 事件由 Arclight 的
 * {@code PrimaryLevelDataMixin} 在 {@code setRaining/setThundering} 内部构造，
 * 正好落在这段作用域里。
 *
 * <p>三个调用点与 Paper 逐条对齐：{@code setWeatherParameters} = COMMAND（/weather）、
 * {@code advanceWeatherCycle} = NATURAL、{@code resetWeatherCycle} = SLEEP（睡过夜）。
 * 插件那条（PLUGIN）在 {@code api.CraftWorldWeatherCauseMixin}。
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelWeatherCauseMixin {

    @WrapOperation(method = "setWeatherParameters",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/ServerLevelData;setRaining(Z)V"))
    private void paperarc$commandRain(ServerLevelData data, boolean raining, Operation<Void> original) {
        PaperarcEventCauses.pushWeather(WeatherChangeEvent.Cause.COMMAND);
        try {
            original.call(data, raining);
        } finally {
            PaperarcEventCauses.popWeather();
        }
    }

    @WrapOperation(method = "setWeatherParameters",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/ServerLevelData;setThundering(Z)V"))
    private void paperarc$commandThunder(ServerLevelData data, boolean thundering, Operation<Void> original) {
        PaperarcEventCauses.pushThunder(ThunderChangeEvent.Cause.COMMAND);
        try {
            original.call(data, thundering);
        } finally {
            PaperarcEventCauses.popThunder();
        }
    }

    @WrapOperation(method = "advanceWeatherCycle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/ServerLevelData;setRaining(Z)V"))
    private void paperarc$naturalRain(ServerLevelData data, boolean raining, Operation<Void> original) {
        PaperarcEventCauses.pushWeather(WeatherChangeEvent.Cause.NATURAL);
        try {
            original.call(data, raining);
        } finally {
            PaperarcEventCauses.popWeather();
        }
    }

    @WrapOperation(method = "advanceWeatherCycle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/ServerLevelData;setThundering(Z)V"))
    private void paperarc$naturalThunder(ServerLevelData data, boolean thundering, Operation<Void> original) {
        PaperarcEventCauses.pushThunder(ThunderChangeEvent.Cause.NATURAL);
        try {
            original.call(data, thundering);
        } finally {
            PaperarcEventCauses.popThunder();
        }
    }

    @WrapOperation(method = "resetWeatherCycle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/ServerLevelData;setRaining(Z)V"))
    private void paperarc$sleepRain(ServerLevelData data, boolean raining, Operation<Void> original) {
        PaperarcEventCauses.pushWeather(WeatherChangeEvent.Cause.SLEEP);
        try {
            original.call(data, raining);
        } finally {
            PaperarcEventCauses.popWeather();
        }
    }

    @WrapOperation(method = "resetWeatherCycle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/ServerLevelData;setThundering(Z)V"))
    private void paperarc$sleepThunder(ServerLevelData data, boolean thundering, Operation<Void> original) {
        PaperarcEventCauses.pushThunder(ThunderChangeEvent.Cause.SLEEP);
        try {
            original.call(data, thundering);
        } finally {
            PaperarcEventCauses.popThunder();
        }
    }
}
