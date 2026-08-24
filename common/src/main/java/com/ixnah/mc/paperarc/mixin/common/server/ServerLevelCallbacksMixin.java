package com.ixnah.mc.paperarc.mixin.common.server;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's EntityAddToWorldEvent / EntityRemoveFromWorldEvent
 * (Entity-AddTo-RemoveFrom-World-Events.patch).
 *
 * Paper fires them at the tail of ServerLevel.EntityCallbacks#onTrackingStart /
 * #onTrackingEnd with (entity.getBukkitEntity(), ServerLevel.this.getWorld()).
 * We use TAIL injections; the Bukkit world is resolved through the bridge
 * (ServerLevel#getWorld is a CraftBukkit-injected method, reflection-based).
 */
@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class ServerLevelCallbacksMixin {

    @Inject(method = "onTrackingStart", at = @At("TAIL"))
    private void paperarc$addToWorld(Entity entity, CallbackInfo ci) {
        PaperArcBridge.fire(new EntityAddToWorldEvent(
                PaperArcBridge.bukkitEntity(entity),
                PaperArcBridge.bukkitWorld((ServerLevel) entity.level())));
    }

    @Inject(method = "onTrackingEnd", at = @At("TAIL"))
    private void paperarc$removeFromWorld(Entity entity, CallbackInfo ci) {
        PaperArcBridge.fire(new EntityRemoveFromWorldEvent(
                PaperArcBridge.bukkitEntity(entity),
                PaperArcBridge.bukkitWorld((ServerLevel) entity.level())));
    }
}
