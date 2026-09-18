package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code DoubleChest} 上的两个 {@code getXxxSide(boolean useSnapshot)}
 * （checklist §1.10 am，第 ⑤ 批）。paper 读私有字段 {@code inventory}，
 * 这里换公开的 {@code getInventory()}。
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
