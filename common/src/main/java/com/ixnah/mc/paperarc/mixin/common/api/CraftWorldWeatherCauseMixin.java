package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Paper {@code Add-cause-to-Weather-ThunderChangeEvents.patch} 的 CraftBukkit 一侧：
 * 插件调 {@code World#setStorm/setThundering} 走的是 PLUGIN。
 * 其余三个原因在 {@code world.ServerLevelWeatherCauseMixin}。
 */
@Mixin(CraftWorld.class)
public abstract class CraftWorldWeatherCauseMixin {

    @WrapOperation(method = "setStorm", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/level/storage/WritableLevelData;setRaining(Z)V"))
    private void paperarc$pluginRain(WritableLevelData data, boolean raining, Operation<Void> original) {
        PaperarcEventCauses.pushWeather(WeatherChangeEvent.Cause.PLUGIN);
        try {
            original.call(data, raining);
        } finally {
            PaperarcEventCauses.popWeather();
        }
    }

    @WrapOperation(method = "setThundering", remap = false,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;setThundering(Z)V"))
    private void paperarc$pluginThunder(PrimaryLevelData data, boolean thundering, Operation<Void> original) {
        PaperarcEventCauses.pushThunder(ThunderChangeEvent.Cause.PLUGIN);
        try {
            original.call(data, thundering);
        } finally {
            PaperarcEventCauses.popThunder();
        }
    }
}
