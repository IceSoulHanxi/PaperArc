package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecutterInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.inventory.StonecutterInventory} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.inventory.StonecutterInventory", remap = false)
public interface StonecutterInventoryIfaceMixin {

    @Unique
    public default ItemStack getInputItem() {
        StonecutterInventory self = (StonecutterInventory) this;
        return self.getItem(0);
    }

    @Unique
    public default void setInputItem(ItemStack itemStack) {
        StonecutterInventory self = (StonecutterInventory) this;
        self.setItem(0, itemStack);
    }

    @Unique
    public default ItemStack getResult() {
        StonecutterInventory self = (StonecutterInventory) this;
        return self.getItem(1);
    }

    @Unique
    public default void setResult(ItemStack itemStack) {
        StonecutterInventory self = (StonecutterInventory) this;
        self.setItem(1, itemStack);
    }
}
