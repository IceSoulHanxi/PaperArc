package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.inventory.CartographyInventory} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.inventory.CartographyInventory", remap = false)
public interface CartographyInventoryIfaceMixin {

    @Unique
    public default ItemStack getResult() {
        CartographyInventory self = (CartographyInventory) this;
        return self.getItem(2);
    }

    @Unique
    public default void setResult(ItemStack newResult) {
        CartographyInventory self = (CartographyInventory) this;
        self.setItem(2, newResult);
    }
}
