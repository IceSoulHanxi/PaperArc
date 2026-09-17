package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A4-4：给 {@code org.bukkit.inventory.SmithingInventory} 补上 paper-api 的 default 方法
 * （运行时接口里一个都没有，插件调用即 NoSuchMethodError）。
 */
@Mixin(targets = "org.bukkit.inventory.SmithingInventory", remap = false)
public interface SmithingInventoryIfaceMixin {

    @Unique
    public default org.bukkit.inventory.ItemStack getInputTemplate() {
        return ((org.bukkit.inventory.Inventory) this).getItem(0);
    }

    @Unique
    public default void setInputTemplate(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(0, item);
    }

    @Unique
    public default org.bukkit.inventory.ItemStack getInputEquipment() {
        return ((org.bukkit.inventory.Inventory) this).getItem(1);
    }

    @Unique
    public default void setInputEquipment(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(1, item);
    }

    @Unique
    public default org.bukkit.inventory.ItemStack getInputMineral() {
        return ((org.bukkit.inventory.Inventory) this).getItem(2);
    }

    @Unique
    public default void setInputMineral(org.bukkit.inventory.ItemStack item) {
        ((org.bukkit.inventory.Inventory) this).setItem(2, item);
    }
}
