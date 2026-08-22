package dev.paperarc.mixin.common.player;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's Add-PlayerInventorySlotChangeEvent patch (event half).
 *
 * Targets {@code ServerPlayer$2}, the anonymous {@code ContainerListener}
 * ServerPlayer registers on its menus (its only job is the INVENTORY_CHANGED
 * criterion trigger). Fires {@link PlayerInventorySlotChangeEvent} before the
 * trigger; if the plugin disables advancement triggering, the vanilla trigger
 * is skipped via cancellation.
 */
@Mixin(targets = "net.minecraft.server.level.ServerPlayer$2")
public class ServerPlayerInventorySlotChangeMixin {

    @Shadow(aliases = "this$0", remap = false)
    @Final
    private ServerPlayer this$0;

    @Inject(
        method = "slotChanged(Lnet/minecraft/world/inventory/AbstractContainerMenu;ILnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void paperarc$onSlotChanged(AbstractContainerMenu menu, int slotId, ItemStack newStack, CallbackInfo ci) {
        Slot slot = menu.getSlot(slotId);
        if (slot instanceof ResultSlot || slot.container != this.this$0.getInventory()) {
            return; // matches vanilla no-op conditions; original body does nothing either way
        }

        ItemStack oldStack = AbstractContainerMenuSlotChangeMixin.PAPERARC_OLD_STACK.get();
        if (oldStack == null) {
            oldStack = newStack;
        }

        PlayerInventorySlotChangeEvent event = new PlayerInventorySlotChangeEvent(
            PaperArcBridge.bukkitPlayer(this.this$0),
            slotId,
            CraftItemStack.asBukkitCopy(oldStack),
            CraftItemStack.asBukkitCopy(newStack)
        );
        PaperArcBridge.fire(event);

        if (!event.shouldTriggerAdvancements()) {
            ci.cancel(); // skip vanilla CriteriaTriggers.INVENTORY_CHANGED.trigger
        }
    }
}
