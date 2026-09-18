package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerExpChangeEvent#getSource()}（给出经验的实体，通常是经验球）。
 *
 * <p>Arclight 在 {@code ExperienceOrb#playerTouch} 里用 {@code @Redirect} 触发这个事件，
 * 调用点只有玩家和数量；经验球本身由 {@code ExperienceOrbExpSourceMixin} 在
 * {@code playerTouch} 的 HEAD 压进 {@link EventCauseState}。
 * 其它路径（插件直接给经验）取不到，返回 {@code null} —— 与 paper 的双参构造器一致。
 */
@Mixin(PlayerExpChangeEvent.class)
public abstract class PlayerExpChangeEventApiMixin {

    @Unique
    private Entity paperarc$source;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;I)V", at = @At("RETURN"), remap = false)
    private void paperarc$captureSource(Player player, int expAmount, CallbackInfo ci) {
        this.paperarc$source = EventCauseState.takeExperienceSource();
    }

    @Unique
    public Entity getSource() {
        return this.paperarc$source;
    }
}
