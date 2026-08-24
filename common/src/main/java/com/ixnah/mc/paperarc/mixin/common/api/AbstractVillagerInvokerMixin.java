package com.ixnah.mc.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.npc.AbstractVillager;

/**
 * Exposes protected AbstractVillager#updateTrades for CraftAbstractVillagerApiMixin#resetOffers.
 * Paper ref: patches/server/Villager-resetOffers.patch (NMS side adds public resetOffers()).
 */
@Mixin(AbstractVillager.class)
public interface AbstractVillagerInvokerMixin {

    @Invoker("updateTrades")
    void paperarc$invokerUpdateTrades();
}
