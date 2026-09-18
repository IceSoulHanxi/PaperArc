package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code PlayerExpChangeEvent#getSource()} 的取值来源：Arclight 在
 * {@code ExperienceOrb#playerTouch} 里用 {@code @Redirect} 触发事件，
 * 调用点拿不到经验球本身，在这里压进 {@link EventCauseState}。
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbExpSourceMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void paperarc$captureExpSource(Player player, CallbackInfo ci) {
        EventCauseState.setExperienceSource(PaperArcBridge.bukkitEntity((ExperienceOrb) (Object) this));
    }

    @Inject(method = "playerTouch", at = @At("RETURN"))
    private void paperarc$clearExpSource(Player player, CallbackInfo ci) {
        EventCauseState.clearExperienceSource();
    }
}
