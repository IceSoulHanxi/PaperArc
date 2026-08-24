package com.ixnah.mc.paperarc.bridge;

import io.papermc.paper.event.block.BlockLockCheckEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;

/**
 * Shared logic for Paper's BlockLockCheckEvent
 * (Add-BlockLockCheckEvent.patch), used by the container/beacon mixins.
 *
 * Deviation: Paper builds the default locked message with
 * PaperAdventure.asAdventure(containerName); we pass the container display
 * name as plain text instead.
 */
public final class ContainerUnlockSupport {

    private ContainerUnlockSupport() {
    }

    public static boolean canUnlockWithEvent(ServerPlayer player, LockCode lock, Component containerName,
                                             BlockEntity blockEntity) {
        org.bukkit.block.Block block = CraftBlock.at(blockEntity.getLevel(), blockEntity.getBlockPos());
        net.kyori.adventure.text.Component lockedMessage = net.kyori.adventure.text.Component.translatable(
                "container.isLocked", net.kyori.adventure.text.Component.text(containerName.getString()));
        net.kyori.adventure.sound.Sound lockedSound = net.kyori.adventure.sound.Sound.sound(
                Sound.BLOCK_CHEST_LOCKED, net.kyori.adventure.sound.Sound.Source.BLOCK, 1.0F, 1.0F);
        BlockLockCheckEvent event =
                new BlockLockCheckEvent(block, PaperArcBridge.bukkitPlayer(player), lockedMessage, lockedSound);
        event.callEvent();
        if (event.getResult() == org.bukkit.event.Event.Result.ALLOW) {
            return true;
        } else if (event.getResult() == org.bukkit.event.Event.Result.DENY
                || (!player.isSpectator() && !lock.unlocksWith(event.isUsingCustomKeyItemStack()
                        ? CraftItemStack.asNMSCopy(event.getKeyItem()) : player.getMainHandItem()))) {
            if (event.getLockedMessage() != null) {
                event.getPlayer().sendActionBar(event.getLockedMessage());
            }
            if (event.getLockedSound() != null) {
                event.getPlayer().playSound(event.getLockedSound());
            }
            return false;
        }
        return true;
    }
}
