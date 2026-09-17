package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.Inventory} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.Inventory", remap = false)
public interface InventoryIfaceMixin {

    @Unique
    public abstract int close();

    @Unique
    public abstract org.bukkit.inventory.InventoryHolder getHolder(boolean p0);

    // 实现体早就在 CraftInventoryApiMixin 上，但接口上一直没有声明 →
    // 插件经 Inventory 接口调用即 NoSuchMethodError（P10a 实测）
    @Unique
    public abstract java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> removeItemAnySlot(
            org.bukkit.inventory.ItemStack... p0);
}
