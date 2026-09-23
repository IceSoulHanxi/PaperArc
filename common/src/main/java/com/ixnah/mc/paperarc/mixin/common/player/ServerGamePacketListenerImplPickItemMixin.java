package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.InventoryPickSlotBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerPickBlockEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.server.network.ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplPickItemMixin {

    @Shadow public ServerPlayer player;

    @Shadow public abstract void send(Packet<?> packet);

    @WrapOperation(
        method = "handlePickItemFromBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryPickItem(Lnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private void paperarc$onPickBlock(net.minecraft.server.network.ServerGamePacketListenerImpl listener, ItemStack stack, Operation<Void> original, ServerboundPickItemFromBlockPacket packet) {
        BlockPos pos = packet.pos();
        org.bukkit.block.Block block = org.bukkit.craftbukkit.v.block.CraftBlock.at(this.player.level(), pos);
        Inventory inventory = this.player.getInventory();
        int targetSlot = inventory.getSuitableHotbarSlot();
        int sourceSlot = inventory.findSlotMatchingItem(stack);
        PlayerPickBlockEvent event = new PlayerPickBlockEvent(
            PaperArcBridge.bukkitPlayer(this.player),
            block,
            packet.includeData(),
            targetSlot,
            sourceSlot
        );
        if (!event.callEvent()) {
            return;
        }
        if (event.getSourceSlot() != sourceSlot || event.getTargetSlot() != targetSlot) {
            ((InventoryPickSlotBridge) (Object) inventory).paperarc$pickSlot(event.getSourceSlot(), event.getTargetSlot());
            this.send(new ClientboundSetHeldSlotPacket(inventory.getSelectedSlot()));
            this.player.inventoryMenu.broadcastChanges();
            return;
        }
        original.call(listener, stack);
    }

    @WrapOperation(
        method = "handlePickItemFromEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryPickItem(Lnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private void paperarc$onPickEntity(net.minecraft.server.network.ServerGamePacketListenerImpl listener, ItemStack stack, Operation<Void> original, ServerboundPickItemFromEntityPacket packet) {
        Entity nmsEntity = this.player.level().getEntityOrPart(packet.id());
        if (nmsEntity == null) {
            return;
        }
        org.bukkit.entity.Entity bukkitEntity = PaperArcBridge.bukkitEntity(nmsEntity);
        Inventory inventory = this.player.getInventory();
        int targetSlot = inventory.getSuitableHotbarSlot();
        int sourceSlot = inventory.findSlotMatchingItem(stack);
        PlayerPickEntityEvent event = new PlayerPickEntityEvent(
            PaperArcBridge.bukkitPlayer(this.player),
            bukkitEntity,
            packet.includeData(),
            targetSlot,
            sourceSlot
        );
        if (!event.callEvent()) {
            return;
        }
        if (event.getSourceSlot() != sourceSlot || event.getTargetSlot() != targetSlot) {
            ((InventoryPickSlotBridge) (Object) inventory).paperarc$pickSlot(event.getSourceSlot(), event.getTargetSlot());
            this.send(new ClientboundSetHeldSlotPacket(inventory.getSelectedSlot()));
            this.player.inventoryMenu.broadcastChanges();
            return;
        }
        original.call(listener, stack);
    }
}
