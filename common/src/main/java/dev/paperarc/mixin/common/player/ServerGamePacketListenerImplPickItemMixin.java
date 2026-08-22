package dev.paperarc.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's Add-PlayerPickItemEvent patch (listener half).
 * <p>
 * Wraps the {@code Inventory#pickSlot(int)} call inside
 * {@code ServerGamePacketListenerImpl#handlePickItem}: fires
 * {@link PlayerPickItemEvent} and applies the (possibly modified) source/target
 * slots. When slots are unchanged the vanilla call runs as-is.
 * <p>
 * Deviation vs Paper: on cancellation Paper returns early, skipping the three
 * inventory sync packets; here they still run but are no-ops because nothing
 * changed. Arclight has no mixin on handlePickItem.
 */
@Mixin(net.minecraft.server.network.ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplPickItemMixin {

    // @formatter:off
    @Shadow public ServerPlayer player;
    // @formatter:on

    @WrapOperation(
        method = "handlePickItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;pickSlot(I)V"
        )
    )
    private void paperarc$onPickItem(Inventory inventory, int slot, Operation<Void> original, ServerboundPickItemPacket packet) {
        int targetSlot = inventory.getSuitableHotbarSlot();
        PlayerPickItemEvent event = new PlayerPickItemEvent(
            PaperArcBridge.bukkitPlayer(this.player),
            targetSlot,
            packet.getSlot()
        );
        if (!event.callEvent()) {
            return;
        }
        if (event.getSourceSlot() == slot && event.getTargetSlot() == targetSlot) {
            original.call(inventory, slot);
        } else {
            ((InventoryPickSlotBridge) (Object) inventory).paperarc$pickSlot(event.getSourceSlot(), event.getTargetSlot());
        }
    }
}
