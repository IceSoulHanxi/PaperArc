package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.block.Lectern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.inventory.LecternInventory} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.inventory.LecternInventory", remap = false)
public interface LecternInventoryIfaceMixin {

    @Unique
    public default ItemStack getBook() {
        LecternInventory self = (LecternInventory) this;
        return self.getItem(0);
    }

    @Unique
    public default void setBook(ItemStack book) {
        LecternInventory self = (LecternInventory) this;
        self.setItem(0, book);
    }
}
