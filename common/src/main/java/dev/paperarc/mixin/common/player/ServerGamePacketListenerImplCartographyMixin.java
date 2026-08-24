package dev.paperarc.mixin.common.player;

import io.papermc.paper.event.player.CartographyItemEvent;
import net.minecraft.world.inventory.CartographyTableMenu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.InventoryView;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mirrors the Paper CartographyItemEvent patch part 1: when clicking the
 * result slot of a cartography table whose result item is present, the fired
 * {@link InventoryClickEvent} is replaced by a {@link CartographyItemEvent}
 * (a subclass, so InventoryClickEvent handlers still receive it).
 *
 * <p>Arclight overwrites {@code handleContainerClick} with CraftBukkit's
 * click logic; these wrappers hook the two {@code new InventoryClickEvent(...)}
 * sites inside that overwrite body.</p>
 */
@Mixin(net.minecraft.server.network.ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplCartographyMixin {

    private static boolean paperarc$isCartographyResult(InventoryView view, int slotNum) {
        if (slotNum != CartographyTableMenu.RESULT_SLOT) {
            return false;
        }
        if (!(view.getTopInventory() instanceof CartographyInventory cartographyInventory)) {
            return false;
        }
        final org.bukkit.inventory.ItemStack result = cartographyInventory.getResult();
        return result != null && !result.isEmpty();
    }

    @WrapOperation(method = "handleContainerClick", at = @At(value = "NEW", remap = false,
        target = "(Lorg/bukkit/inventory/InventoryView;Lorg/bukkit/event/inventory/InventoryType$SlotType;ILorg/bukkit/event/inventory/ClickType;Lorg/bukkit/event/inventory/InventoryAction;)Lorg/bukkit/event/inventory/InventoryClickEvent;"))
    private InventoryClickEvent paperarc$cartographyItemEvent(InventoryView view, InventoryType.SlotType type, int slotNum,
                                                             ClickType click, InventoryAction action,
                                                             Operation<InventoryClickEvent> original) {
        if (paperarc$isCartographyResult(view, slotNum)) {
            return new CartographyItemEvent(view, type, slotNum, click, action);
        }
        return original.call(view, type, slotNum, click, action);
    }

    @WrapOperation(method = "handleContainerClick", at = @At(value = "NEW", remap = false,
        target = "(Lorg/bukkit/inventory/InventoryView;Lorg/bukkit/event/inventory/InventoryType$SlotType;ILorg/bukkit/event/inventory/ClickType;Lorg/bukkit/event/inventory/InventoryAction;I)Lorg/bukkit/event/inventory/InventoryClickEvent;"))
    private InventoryClickEvent paperarc$cartographyItemEventNumberKey(InventoryView view, InventoryType.SlotType type, int slotNum,
                                                                      ClickType click, InventoryAction action, int button,
                                                                      Operation<InventoryClickEvent> original) {
        if (paperarc$isCartographyResult(view, slotNum)) {
            return new CartographyItemEvent(view, type, slotNum, click, action, button);
        }
        return original.call(view, type, slotNum, click, action, button);
    }
}
