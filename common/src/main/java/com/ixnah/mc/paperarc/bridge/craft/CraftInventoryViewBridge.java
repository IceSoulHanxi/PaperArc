package com.ixnah.mc.paperarc.bridge.craft;

import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Duck-typing bridge merged onto {@code CraftInventoryView} by its provider
 * mixin. The generic {@code container} field and the {@code getHandle()}
 * accessor erase to AbstractContainerMenu; view api mixins cast to this
 * interface to reach them (shadowing inherited members on Arclight's
 * runtime-generated CB classes does not resolve).
 */
public interface CraftInventoryViewBridge {

    AbstractContainerMenu paperarc$menu();
}
