package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.entity.CraftAbstractVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Adds resetOffers missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Villager-resetOffers.patch.
 */
@Mixin(CraftAbstractVillager.class)
public abstract class CraftAbstractVillagerApiMixin {

    @Shadow
    public abstract AbstractVillager getHandle();

    @Unique
    public void resetOffers() {
        AbstractVillager handle = this.getHandle();
        handle.overrideOffers(new MerchantOffers());
        ((AbstractVillagerInvokerMixin) handle).paperarc$invokerUpdateTrades();
    }
}
