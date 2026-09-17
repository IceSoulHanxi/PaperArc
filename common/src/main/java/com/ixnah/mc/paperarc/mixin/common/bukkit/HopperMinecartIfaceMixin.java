package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.minecart.HopperMinecart} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code com.destroystokyo.paper.loottable.LootableEntityInventory}。终端方法在 CraftLootableEntityApiMixin 上（自动补货在 Arclight 上不可用，见 docs/gaps.md）。</p>
 */
@Mixin(targets = "org.bukkit.entity.minecart.HopperMinecart", remap = false)
public interface HopperMinecartIfaceMixin extends com.destroystokyo.paper.loottable.LootableEntityInventory {

    @Unique
    public abstract int getPickupCooldown();

    @Unique
    public abstract void setPickupCooldown(int p0);

}