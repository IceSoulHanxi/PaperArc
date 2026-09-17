package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.bukkit.craftbukkit.v.CraftGameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code GameEvent#getRange()/getVibrationLevel()}（Add-more-game-event-API）。 */
@Mixin(CraftGameEvent.class)
public abstract class CraftGameEventApiMixin {

    @Shadow
    public abstract net.minecraft.world.level.gameevent.GameEvent getHandle();

    @Unique
    public int getRange() {
        return this.getHandle().notificationRadius();
    }

    @Unique
    public int getVibrationLevel() {
        // Paper 读的是 VibrationSystem 的频率表；1.21.1 的重载收 ResourceKey/Holder，
        // 从注册表反查这个 GameEvent 的 key 再查频率，查不到按 0（不产生振动）返回。
        return net.minecraft.core.registries.BuiltInRegistries.GAME_EVENT
                .getResourceKey(this.getHandle())
                .map(VibrationSystem::getGameEventFrequency)
                .orElse(0);
    }
}
