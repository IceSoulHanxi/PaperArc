package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code PlayerExpChangeEvent#getSource()} 的取值来源：事件在
 * {@code ExperienceOrb#playerTouch} 里由 Arclight 触发，构造点拿不到经验球本身。
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbExpSourceMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void paperarc$captureExpSource(Player player, CallbackInfo ci) {
        PaperarcEventCauses.pushExperienceSource(PaperArcBridge.bukkitEntity((ExperienceOrb) (Object) this));
    }

    @Inject(method = "playerTouch", at = @At("RETURN"))
    private void paperarc$clearExpSource(Player player, CallbackInfo ci) {
        PaperarcEventCauses.popExperienceSource();
    }
}
