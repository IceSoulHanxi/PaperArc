package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.InventoryPickSlotBridge;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

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
    @Accessor("items") @Final public abstract NonNullList<ItemStack> paperarc$getItems();
    @Accessor("selected") public abstract int paperarc$getSelected();
    @Accessor("selected") public abstract void paperarc$setSelected(int value);
    // @formatter:on

    @Override
    public void paperarc$pickSlot(int slot, int targetSlot) {
        this.paperarc$setSelected(targetSlot);
        ItemStack itemstack = this.paperarc$getItems().get(this.paperarc$getSelected());
        this.paperarc$getItems().set(this.paperarc$getSelected(), this.paperarc$getItems().get(slot));
        this.paperarc$getItems().set(slot, itemstack);
    }
}
