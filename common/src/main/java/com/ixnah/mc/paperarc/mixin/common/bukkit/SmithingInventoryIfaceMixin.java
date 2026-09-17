package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.inventory.SmithingInventory} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.inventory.SmithingInventory", remap = false)
public interface SmithingInventoryIfaceMixin {

    @Unique
    public default ItemStack getInputTemplate() {
        SmithingInventory self = (SmithingInventory) this;
        return self.getItem(0);
    }

    @Unique
    public default void setInputTemplate(ItemStack itemStack) {
        SmithingInventory self = (SmithingInventory) this;
        self.setItem(0, itemStack);
    }

    @Unique
    public default ItemStack getInputEquipment() {
        SmithingInventory self = (SmithingInventory) this;
        return self.getItem(1);
    }

    @Unique
    public default void setInputEquipment(ItemStack itemStack) {
        SmithingInventory self = (SmithingInventory) this;
        self.setItem(1, itemStack);
    }

    @Unique
    public default ItemStack getInputMineral() {
        SmithingInventory self = (SmithingInventory) this;
        return self.getItem(2);
    }

    @Unique
    public default void setInputMineral(ItemStack itemStack) {
        SmithingInventory self = (SmithingInventory) this;
        self.setItem(2, itemStack);
    }
}
