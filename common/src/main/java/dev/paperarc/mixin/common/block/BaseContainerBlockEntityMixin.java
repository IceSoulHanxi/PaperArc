package dev.paperarc.mixin.common.block;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.block.BlockLockCheckEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's BlockLockCheckEvent
 * (Add-BlockLockCheckEvent.patch).
 *
 * Paper adds a static canUnlock(Player, LockCode, Component, BlockEntity)
 * overload that fires the event for ServerPlayers on a valid block entity and
 * routes canOpen through it. Since we cannot add overloads, we inject into
 * {@code canOpen} at HEAD and replicate the Paper logic; the shared logic
 * lives in {@link #paperarc$canUnlockWithEvent} so BeaconBlockEntityMixin can
 * reuse it (see BeaconBlockEntityMixin for the beacon path).
 */
@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin {

    @Shadow
    private LockCode lockKey;

    @Inject(method = "canOpen", at = @At("HEAD"), cancellable = true)
    private void paperarc$blockLockCheck(Player player, CallbackInfoReturnable<Boolean> cir) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        Level level = blockEntity.getLevel();
        if (!(player instanceof ServerPlayer serverPlayer) || level == null
                || level.getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            return; // matches Paper: fall back to vanilla logic
        }
        cir.setReturnValue(paperarc$canUnlockWithEvent(
                serverPlayer, this.lockKey, ((BaseContainerBlockEntity) (Object) this).getDisplayName(), blockEntity));
    }

    /**
     * Replicates Paper's canUnlock(Player, LockCode, Component, BlockEntity).
     *
     * Deviation: Paper builds the default locked message with
     * PaperAdventure.asAdventure(containerName); PaperAdventure is unavailable
     * here, so the container display name is passed as plain text.
     */
    public static boolean paperarc$canUnlockWithEvent(ServerPlayer player, LockCode lock, Component containerName,
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
