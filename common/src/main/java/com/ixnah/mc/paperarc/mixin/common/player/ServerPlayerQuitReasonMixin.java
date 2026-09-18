package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.ServerPlayerQuitReasonBridge;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code ServerPlayer#quitReason}，默认 {@code DISCONNECTED}（正常退出）。 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerQuitReasonMixin implements ServerPlayerQuitReasonBridge {

    @Unique
    private PlayerQuitEvent.QuitReason paperarc$quitReason = PlayerQuitEvent.QuitReason.DISCONNECTED;

    @Override
    public PlayerQuitEvent.QuitReason paperarc$getQuitReason() {
        return this.paperarc$quitReason;
    }

    @Override
    public void paperarc$setQuitReason(PlayerQuitEvent.QuitReason reason) {
        this.paperarc$quitReason = reason;
    }
}
