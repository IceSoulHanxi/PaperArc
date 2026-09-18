package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
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
 * <p>事件在 {@code ExperienceOrb#playerTouch} 里由 Arclight 触发，构造点只有玩家和数量；
 * 经验球本身由 {@code entity.ExperienceOrbExpSourceMixin} 在 HEAD 压 ThreadLocal。
 * 其它路径（插件直接给经验）取不到，返回 {@code null} —— 与 paper 的双参构造器一致。
 */
@Mixin(PlayerExpChangeEvent.class)
public abstract class PlayerExpChangeEventApiMixin {

    @Unique
    private Entity paperarc$source;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;I)V", at = @At("RETURN"), remap = false)
    private void paperarc$captureSource(Player player, int expAmount, CallbackInfo ci) {
        this.paperarc$source = PaperarcEventCauses.takeExperienceSource();
    }

    @Unique
    public Entity getSource() {
        return this.paperarc$source;
    }
}
