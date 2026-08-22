package dev.paperarc.mixin.common.player;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the private entity id of {@link ServerboundInteractPacket} for
 * PlayerUseUnknownEntityEvent support.
 */
@Mixin(ServerboundInteractPacket.class)
public interface ServerboundInteractPacketAccessor {

    @Accessor("entityId")
    int paperarc$entityId();
}
