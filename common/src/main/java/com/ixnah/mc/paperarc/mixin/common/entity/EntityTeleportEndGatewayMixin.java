package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.block.CraftEndGateway;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-EntityTeleportEndGatewayEvent.patch.
 *
 * <p>In Paper, the CraftBukkit teleport-event block inside
 * {@code Entity#changeDimension(DimensionTransition)} branches on the End
 * Gateway condition and fires a dedicated {@link EntityTeleportEndGatewayEvent}
 * (carrying the gateway block) instead of the generic
 * {@code EntityTeleportEvent}.
 *
 * <p>On Arclight there is no such call site in vanilla bytecode: Arclight's own
 * {@code EntityMixin} decorates {@code changeDimension} at HEAD via a handler
 * method {@code arclight$changeDim}, and the
 * {@code CraftEventFactory#callEntityTeleportEvent} invocation lives inside
 * that merged handler. We therefore wrap the invocation inside
 * {@code arclight$changeDim} and substitute the Paper event when the gateway
 * condition matches; because {@code EntityTeleportEndGatewayEvent extends
 * EntityTeleportEvent}, the surrounding logic (cancel check + destination
 * adoption) keeps working unchanged.
 *
 * <p>{@code require = 0} keeps boot safe if the handler is absent (config order
 * or upstream refactor): the event simply won't fire instead of crashing.
 */
@Mixin(Entity.class)
public abstract class EntityTeleportEndGatewayMixin {

    @Shadow
    public net.minecraft.world.entity.PortalProcessor portalProcess;

    @Shadow
    public abstract Level level();

    @WrapOperation(
            method = "arclight$changeDim(Lnet/minecraft/world/level/portal/DimensionTransition;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/bukkit/craftbukkit/v/event/CraftEventFactory;callEntityTeleportEvent(Lnet/minecraft/world/entity/Entity;Lorg/bukkit/Location;)Lorg/bukkit/event/entity/EntityTeleportEvent;",
                    remap = false
            ),
            require = 0
    )
    private EntityTeleportEvent paperarc$fireGatewayTeleportEvent(Entity instance, Location to,
                                                                 Operation<EntityTeleportEvent> original) {
        if (this.portalProcess != null
                && this.portalProcess.isSamePortal((net.minecraft.world.level.block.Portal) net.minecraft.world.level.block.Blocks.END_GATEWAY)
                && this.level().getBlockEntity(this.portalProcess.getEntryPosition())
                        instanceof TheEndGatewayBlockEntity gatewayBlockEntity) {
            var bukkit = PaperArcBridge.bukkitEntity((Entity) (Object) this);
            var event = new EntityTeleportEndGatewayEvent(
                    bukkit, bukkit.getLocation(), to,
                    new CraftEndGateway(to.getWorld(), gatewayBlockEntity));
            event.callEvent();
            return event;
        }
        return original.call(instance, to);
    }
}
