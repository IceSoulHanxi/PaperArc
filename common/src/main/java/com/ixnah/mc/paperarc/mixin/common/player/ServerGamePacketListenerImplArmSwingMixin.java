package com.ixnah.mc.paperarc.mixin.common.player;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PlayerArmSwingEvent 触发点。
 * <p>
 * 对照 Paper：在 handleAnimate（ServerboundSwingPacket）中原 PlayerAnimationEvent
 * 处改为发 PlayerArmSwingEvent（携带 EquipmentSlot），取消时不执行挥手。
 * <p>
 * 冲突评估：handleAnimate 未被 PaperArc 既有 mixin 或 Arclight 占用
 * （Arclight 对 ServerGamePacketListenerImpl 的占用集中在 handleMovePlayer/
 * disconnect 等），HEAD 可安全注入。
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplArmSwingMixin {

    @Inject(
        method = "handleAnimate(Lnet/minecraft/network/protocol/game/ServerboundSwingPacket;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void paperarc$onArmSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;
        Player bukkit = Bukkit.getPlayer(self.player.getUUID());
        if (bukkit == null) {
            return;
        }
        PlayerArmSwingEvent event = new PlayerArmSwingEvent(
            bukkit, CraftEquipmentSlot.getHand(packet.getHand()));
        if (!event.callEvent()) {
            ci.cancel();
        }
    }
}
