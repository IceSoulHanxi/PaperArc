package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code BlockDamageEvent#getBlockFace()} 的取值来源：
 * {@code ServerPlayerGameMode#handleBlockBreakAction} 的形参里有玩家点的那个面，
 * 而 {@code CraftEventFactory#callBlockDamageEvent} 的形参里没有。
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeBlockFaceMixin {

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void paperarc$captureBlockFace(BlockPos pos, ServerboundPlayerActionPacket.Action action,
                                           Direction direction, int worldHeight, int sequence,
                                           CallbackInfo ci) {
        PaperarcEventCauses.pushBlockDamageFace(CraftBlock.notchToBlockFace(direction));
    }

    @Inject(method = "handleBlockBreakAction", at = @At("RETURN"))
    private void paperarc$clearBlockFace(BlockPos pos, ServerboundPlayerActionPacket.Action action,
                                         Direction direction, int worldHeight, int sequence,
                                         CallbackInfo ci) {
        PaperarcEventCauses.popBlockDamageFace();
    }
}
