package dev.paperarc.mixin.common.player;

/**
 * Duck bridge exposing Paper's added {@code Inventory#pickSlot(int, int)} overload
 * (from the Add-PlayerPickItemEvent patch) under a {@code paperarc$} name.
 */
public interface InventoryPickSlotBridge {

    void paperarc$pickSlot(int slot, int targetSlot);
}
