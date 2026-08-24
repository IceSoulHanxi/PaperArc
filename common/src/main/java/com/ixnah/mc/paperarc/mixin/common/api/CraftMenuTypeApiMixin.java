package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.CraftMenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Adds Adventure create overload missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Adventure.patch (CraftMenuType#create(HumanEntity, Component));
 * here inverted: serialize Component to legacy String and delegate to the existing
 * String-based create.
 */
@Mixin(CraftMenuType.class)
public abstract class CraftMenuTypeApiMixin {

    @Shadow
    public abstract org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity player, String title);

    @Unique
    public org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity player, Component title) {
        return this.create(player, LegacyComponentSerializer.legacySection().serialize(title));
    }
}
