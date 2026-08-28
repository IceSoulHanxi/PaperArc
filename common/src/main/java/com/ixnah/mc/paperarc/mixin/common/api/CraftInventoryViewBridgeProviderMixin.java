package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftInventoryViewBridge;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Merges {@link CraftInventoryViewBridge} onto the generic
 * {@code CraftInventoryView<T>} base. The erased container field and the
 * getHandle() accessor both expose the menu as AbstractContainerMenu.
 */
@Mixin(CraftInventoryView.class)
public abstract class CraftInventoryViewBridgeProviderMixin implements CraftInventoryViewBridge {

    @Shadow
    protected AbstractContainerMenu container;

    @Shadow
    public abstract AbstractContainerMenu getHandle();

    @Override
    public AbstractContainerMenu paperarc$menu() {
        return this.getHandle();
    }
}
