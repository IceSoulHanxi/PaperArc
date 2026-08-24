package com.ixnah.mc.paperarc.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-PlayerInventorySlotChangeEvent patch (helper half).
 *
 * Vanilla's {@code AbstractContainerMenu#triggerSlotListeners} replaces the
 * remote-slot copy ({@code remoteSlots.set(...)}) with the NEW stack before
 * notifying listeners, so the old value is unrecoverable at listener time.
 * We wrap the {@code NonNullList#set} call to capture the previous stack and
 * expose it to {@link ServerPlayerInventorySlotChangeMixin} via a ThreadLocal.
 */
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuSlotChangeMixin {

    /** Old (pre-change) item stack for the slot-change currently being broadcast. */
    @Unique
    private static final ThreadLocal<ItemStack> PAPERARC_OLD_STACK = new ThreadLocal<>();

    @WrapOperation(
        method = "triggerSlotListeners",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private Object paperarc$captureOldStack(NonNullList<ItemStack> remoteSlots, int slotId, Object newStack,
                                            Operation<Object> original) {
        com.ixnah.mc.paperarc.bridge.MenuSlotState.OLD_STACK.set(remoteSlots.get(slotId));
        return original.call(remoteSlots, slotId, newStack);
    }
}
