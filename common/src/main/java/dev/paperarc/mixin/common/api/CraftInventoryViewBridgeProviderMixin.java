package dev.paperarc.mixin.common.api;

import dev.paperarc.bridge.craft.CraftInventoryViewBridge;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryView;
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
