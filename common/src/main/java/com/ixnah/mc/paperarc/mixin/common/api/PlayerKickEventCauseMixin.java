package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** paper 的 {@code PlayerKickEvent#getCause()}；原因来源见 {@code PaperarcEventCauses}。 */
@Mixin(PlayerKickEvent.class)
public abstract class PlayerKickEventCauseMixin {

    @Unique
    private PlayerKickEvent.Cause paperarc$cause = PlayerKickEvent.Cause.UNKNOWN;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Ljava/lang/String;Ljava/lang/String;)V", at = @At("RETURN"))
    private void paperarc$captureCause(Player player, String kickReason, String leaveMessage, CallbackInfo ci) {
        this.paperarc$cause = PaperarcEventCauses.kick();
    }

    @Unique
    public PlayerKickEvent.Cause getCause() {
        return this.paperarc$cause;
    }
}
