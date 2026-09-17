package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.inventory.GrindstoneInventory} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.inventory.GrindstoneInventory", remap = false)
public interface GrindstoneInventoryIfaceMixin {

    @Unique
    public default ItemStack getUpperItem() {
        GrindstoneInventory self = (GrindstoneInventory) this;
        return self.getItem(0);
    }

    @Unique
    public default void setUpperItem(ItemStack upperItem) {
        GrindstoneInventory self = (GrindstoneInventory) this;
        self.setItem(0, upperItem);
    }

    @Unique
    public default ItemStack getLowerItem() {
        GrindstoneInventory self = (GrindstoneInventory) this;
        return self.getItem(1);
    }

    @Unique
    public default void setLowerItem(ItemStack lowerItem) {
        GrindstoneInventory self = (GrindstoneInventory) this;
        self.setItem(1, lowerItem);
    }

    @Unique
    public default ItemStack getResult() {
        GrindstoneInventory self = (GrindstoneInventory) this;
        return self.getItem(2);
    }

    @Unique
    public default void setResult(ItemStack result) {
        GrindstoneInventory self = (GrindstoneInventory) this;
        self.setItem(2, result);
    }
}
