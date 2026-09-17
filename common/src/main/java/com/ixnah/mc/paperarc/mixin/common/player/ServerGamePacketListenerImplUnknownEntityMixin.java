package com.ixnah.mc.paperarc.mixin.common.player;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
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

    @Accessor("player")
    abstract ServerPlayer paperarc$getPlayer();

    @Inject(method = "handleInteract", at = @At("TAIL"))
    private void paperarc$useUnknownEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (packet.getTarget(this.paperarc$getPlayer().serverLevel()) != null) {
            return; // known entity: vanilla interaction pipeline handled it
        }
        final ServerPlayer sender = this.paperarc$getPlayer();
        final int entityId = ((ServerboundInteractPacketAccessor) packet).paperarc$entityId();
        // Handler 实现在 bridge/：mixin 的匿名类会被 Mixin 搬进目标类，搬完的
        // InnerClasses/NestHost 仍指向原 mixin 外围类，与目标类互相矛盾
        // （checklist §1.6 c，main 62ae973）。
        packet.dispatch(new com.ixnah.mc.paperarc.bridge.PaperarcUnknownEntityHandler(sender, entityId));
    }
}
