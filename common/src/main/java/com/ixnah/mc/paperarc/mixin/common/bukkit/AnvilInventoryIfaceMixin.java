package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.inventory.AnvilInventory} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.inventory.AnvilInventory", remap = false)
public interface AnvilInventoryIfaceMixin {

    @Unique
    public default ItemStack getFirstItem() {
        AnvilInventory self = (AnvilInventory) this;
        return self.getItem(0);
    }

    @Unique
    public default void setFirstItem(ItemStack firstItem) {
        AnvilInventory self = (AnvilInventory) this;
        self.setItem(0, firstItem);
    }

    @Unique
    public default ItemStack getSecondItem() {
        AnvilInventory self = (AnvilInventory) this;
        return self.getItem(1);
    }

    @Unique
    public default void setSecondItem(ItemStack secondItem) {
        AnvilInventory self = (AnvilInventory) this;
        self.setItem(1, secondItem);
    }

    @Unique
    public default ItemStack getResult() {
        AnvilInventory self = (AnvilInventory) this;
        return self.getItem(2);
    }

    @Unique
    public default void setResult(ItemStack result) {
        AnvilInventory self = (AnvilInventory) this;
        self.setItem(2, result);
    }
}
