package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.block.CraftEndGateway;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's {@code Add-EntityTeleportEndGatewayEvent.patch}.
 *
 * <p>Paper branches the CraftBukkit teleport-event block inside
 * {@code Entity#changeDimension(DimensionTransition)}: when the portal in use
 * is an End Gateway it fires {@link EntityTeleportEndGatewayEvent} (carrying
 * the gateway block state) instead of the plain {@code EntityTeleportEvent}.
 * The Paper event extends the plain one, so swapping the instance keeps the
 * surrounding cancel check and destination adoption working untouched.
 *
 * <p>We cannot inject into {@code Entity#changeDimension} — see the Y-1 note
 * below — so the swap happens one level down, at the single
 * {@code new EntityTeleportEvent(...)} inside
 * {@code CraftEventFactory#callEntityTeleportEvent(Entity, Location)}, which is
 * the only place that event is constructed at runtime. The gateway condition is
 * re-derived from the entity's {@code portalProcess}, so the other caller
 * ({@code callEntityTeleportEvent(Entity, double, double, double)}, used by
 * tamed-animal teleports) is unaffected.
 *
 * <p><b>Y-1: why anchoring {@code Entity#changeDimension} is impossible.</b>
 * Arclight injects that whole CraftBukkit block with its own
 * {@code io.izzel.arclight.mixin.Decorate(method = "changeDimension",
 * inject = true, at = @At("HEAD"))}. The spliced body only appears in the
 * target during Arclight's decoration post-processing, which runs after every
 * third-party injector; at our injection time the applied class still has the
 * handler sitting in a separate, Mixin-renamed method
 * ({@code decorate$<sessionId>$arclight$changeDim}, name not stable enough to
 * select). Raising {@code @Mixin(priority)} does not change this: our config is
 * already priority 1100 against Arclight's 500, and a run at priority 2000 gave
 * the identical {@code Scanned 0 target(s)}.
 */
@Mixin(CraftEventFactory.class)
public abstract class CraftEventFactoryEndGatewayMixin {

    @WrapOperation(
            method = "callEntityTeleportEvent(Lnet/minecraft/world/entity/Entity;Lorg/bukkit/Location;)Lorg/bukkit/event/entity/EntityTeleportEvent;",
            at = @At(
                    value = "NEW",
                    target = "(Lorg/bukkit/entity/Entity;Lorg/bukkit/Location;Lorg/bukkit/Location;)Lorg/bukkit/event/entity/EntityTeleportEvent;",
                    remap = false
            )
    )
    private static EntityTeleportEvent paperarc$useEndGatewayTeleportEvent(org.bukkit.entity.Entity what,
                                                                          Location from, Location to,
                                                                          Operation<EntityTeleportEvent> original) {
        if (what instanceof CraftEntity craft && to != null && to.getWorld() != null) {
            net.minecraft.world.entity.Entity handle = craft.getHandle();
            PortalProcessor processor = handle.portalProcess;
            if (processor != null
                    && processor.isSamePortal((Portal) Blocks.END_GATEWAY)
                    && handle.level().getBlockEntity(processor.getEntryPosition()) instanceof TheEndGatewayBlockEntity gateway) {
                return new EntityTeleportEndGatewayEvent(what, from, to,
                        new CraftEndGateway(to.getWorld(), gateway));
            }
        }
        return original.call(what, from, to);
    }
}
