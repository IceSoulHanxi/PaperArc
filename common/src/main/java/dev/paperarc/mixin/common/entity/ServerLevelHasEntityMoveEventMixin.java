package dev.paperarc.mixin.common.entity;

import dev.paperarc.bridge.ServerLevelMoveEventBridge;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mirrors the flag Paper maintains in {@code ServerLevel#hasEntityMoveEvent}:
 * refreshed at the head of every level tick from the registered listener
 * count, so entities skip the event entirely when no plugin listens.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelHasEntityMoveEventMixin implements ServerLevelMoveEventBridge {

    @Unique
    private boolean paperarc$hasEntityMoveEvent = true;

    @Override
    public boolean paperarc$hasEntityMoveEvent() {
        return this.paperarc$hasEntityMoveEvent;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void paperarc$refreshMoveEventFlag(CallbackInfo ci) {
        this.paperarc$hasEntityMoveEvent = EntityMoveEvent.getHandlerList().getRegisteredListeners().length > 0;
    }
}
