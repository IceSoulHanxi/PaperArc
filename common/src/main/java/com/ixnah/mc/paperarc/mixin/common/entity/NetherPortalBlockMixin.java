package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.sugar.Local;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.PortalType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's EntityPortalReadyEvent (Add-EntityPortalReadyEvent.patch),
 * 1.20.1 form.
 *
 * <p>1.20.1 fires the event inside {@code Entity#handleNetherPortal()} right
 * after {@code portalTime = i} and before {@code setPortalCooldown()}; the
 * target world is the local {@code ServerLevel worldserver1} computed from the
 * exit-portal lookup. Cancelling resets portalTime (matching Paper's
 * {@code this.portalTime = 0}) so the entity stays in the portal.
 *
 * <p>Deviation vs Paper: {@code setTargetWorld} re-routing is not applied.
 */
@Mixin(Entity.class)
public abstract class NetherPortalBlockMixin {

    @Shadow
    protected int portalTime;

    @Inject(
            method = "handleNetherPortal",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPortalCooldown()V"),
            cancellable = true
    )
    private void paperarc$portalReady(CallbackInfo ci, @Local(ordinal = 1) ServerLevel worldserver1) {
        Entity self = (Entity) (Object) this;
        EntityPortalReadyEvent event = new EntityPortalReadyEvent(
                PaperArcBridge.bukkitEntity(self),
                worldserver1 == null ? null : PaperArcBridge.bukkitWorld(worldserver1),
                PortalType.NETHER);
        if (!event.callEvent()) {
            this.portalTime = 0;
            ci.cancel();
        }
    }
}
