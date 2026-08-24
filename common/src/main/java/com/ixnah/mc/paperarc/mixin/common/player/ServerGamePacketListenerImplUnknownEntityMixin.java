package com.ixnah.mc.paperarc.mixin.common.player;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.util.CraftVector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires Paper's PlayerUseUnknownEntityEvent when the client interacts with an
 * entity id that does not resolve to a real entity (virtual entities).
 *
 * <p>Mirrors the Paper patch: the added {@code else} branch in
 * {@code handleInteract} dispatches a Handler that reports the event. Here we
 * re-resolve the target at TAIL and only fire when no entity was found.</p>
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplUnknownEntityMixin {

    @Shadow
    protected ServerPlayer player;

    @Inject(method = "handleInteract", at = @At("TAIL"))
    private void paperarc$useUnknownEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (packet.getTarget(this.player.serverLevel()) != null) {
            return; // known entity: vanilla interaction pipeline handled it
        }
        final ServerPlayer sender = this.player;
        final int entityId = ((ServerboundInteractPacketAccessor) packet).paperarc$entityId();
        packet.dispatch(new ServerboundInteractPacket.Handler() {
            @Override
            public void onInteraction(InteractionHand hand) {
                fire(sender, entityId, false, hand, null);
            }

            @Override
            public void onInteraction(InteractionHand hand, Vec3 pos) {
                fire(sender, entityId, false, hand, pos);
            }

            @Override
            public void onAttack() {
                fire(sender, entityId, true, InteractionHand.MAIN_HAND, null);
            }
        });
    }

    private static void fire(ServerPlayer player, int entityId, boolean isAttack, InteractionHand hand, Vec3 pos) {
        new PlayerUseUnknownEntityEvent(
            PaperArcBridge.bukkitPlayer(player),
            entityId,
            isAttack,
            CraftEquipmentSlot.getHand(hand),
            pos != null ? CraftVector.toBukkit(pos) : null
        ).callEvent();
    }
}
