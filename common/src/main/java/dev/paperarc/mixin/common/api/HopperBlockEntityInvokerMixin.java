package dev.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.block.entity.HopperBlockEntity;

/**
 * Exposes HopperBlockEntity's private cooldown storage for
 * {@code CraftHopperApiMixin#getTransferCooldown/setTransferCooldown}.
 * Paper ref: patches/server/Expanded-Hopper-API.patch (ATs:
 * {@code setCooldown(I)V} / {@code cooldownTime}).
 */
@Mixin(HopperBlockEntity.class)
public interface HopperBlockEntityInvokerMixin {

    @Accessor("cooldownTime")
    int paperarc$cooldownTime();

    @Invoker("setCooldown")
    void paperarc$invokeSetCooldown(int cooldown);
}
