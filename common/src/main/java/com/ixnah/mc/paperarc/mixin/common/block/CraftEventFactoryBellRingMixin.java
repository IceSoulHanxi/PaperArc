package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.papermc.paper.event.block.BellRingEvent;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-BellRingEvent.patch.
 *
 * <p>Paper swaps the deprecated {@code org.bukkit.event.block.BellRingEvent}
 * constructed by CraftEventFactory#handleBellRingEvent for
 * {@code io.papermc.paper.event.block.BellRingEvent} (a subclass sharing the
 * same HandlerList). Arclight's BellBlockMixin routes the ring through
 * {@code handleBellRingEvent} already, so wrapping the constructor invocation
 * upgrades the fired event type without touching any injection point Arclight
 * owns; firing/cancellation stay handled by the existing CraftBukkit code path.
 *
 * <p>NOTE FOR INTEGRATOR: this targets a CraftBukkit (generated) class. If the
 * runtime mixin pipeline does not transform {@code org.bukkit.craftbukkit.v1_20_R1.*},
 * this mixin silently no-ops and the deprecated event type keeps firing — safe
 * degradation, verify at integration time.
 */
@Mixin(CraftEventFactory.class)
public abstract class CraftEventFactoryBellRingMixin {

    @WrapOperation(
            method = "handleBellRingEvent",
            remap = false,
            at = @At(
                    value = "NEW",
                    target = "(Lorg/bukkit/block/Block;Lorg/bukkit/block/BlockFace;Lorg/bukkit/entity/Entity;)Lorg/bukkit/event/block/BellRingEvent;",
                    remap = false
            )
    )
    private static org.bukkit.event.block.BellRingEvent paperarc$useModernBellRingEvent(org.bukkit.block.Block block,
                                                                                BlockFace direction,
                                                                                Entity entity,
                                                                                Operation<org.bukkit.event.block.BellRingEvent> original) {
        return new BellRingEvent(block, direction, entity);
    }
}
