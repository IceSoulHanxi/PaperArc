package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.InventoryPickSlotBridge;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Adds Paper's {@code pickSlot(int slot, int targetSlot)} overload
 * (Add-PlayerPickItemEvent patch) to {@link Inventory} under a
 * {@code paperarc$} name; body replicates the patched vanilla method exactly.
 * <p>
 * Arclight's InventoryMixin does not touch pickSlot/selected.
 */
@Mixin(Inventory.class)
public abstract class InventoryPickSlotMixin implements InventoryPickSlotBridge {

    // @formatter:off
    @Shadow @Final public NonNullList<ItemStack> items;
    @Shadow public int selected;
    // @formatter:on

    @Override
    public void paperarc$pickSlot(int slot, int targetSlot) {
        this.selected = targetSlot;
        ItemStack itemstack = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(slot));
        this.items.set(slot, itemstack);
    }
}
