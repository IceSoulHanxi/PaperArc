package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code DoubleChest#getLeftSide(boolean)/getRightSide(boolean)}（A6/X-2 第五批）：
 * 带 {@code useSnapshot} 的重载，转发给两侧 inventory 的 {@code getHolder(boolean)}。
 */
@Mixin(DoubleChest.class)
public abstract class DoubleChestApiMixin {

    @Unique
    private DoubleChestInventory paperarc$inventory() {
        return (DoubleChestInventory) ((DoubleChest) (Object) this).getInventory();
    }

    @Unique
    public InventoryHolder getLeftSide(boolean useSnapshot) {
        return this.paperarc$inventory().getLeftSide().getHolder(useSnapshot);
    }

    @Unique
    public InventoryHolder getRightSide(boolean useSnapshot) {
        return this.paperarc$inventory().getRightSide().getHolder(useSnapshot);
    }
}
