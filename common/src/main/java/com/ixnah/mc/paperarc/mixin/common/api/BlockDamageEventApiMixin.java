package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code BlockDamageEvent#getBlockFace()}。
 *
 * <p>{@code CraftEventFactory#callBlockDamageEvent} 的形参里没有朝向，但它唯一的上游
 * {@code ServerPlayerGameMode#handleBlockBreakAction} 的形参里有
 * （见 {@code player.ServerPlayerGameModeBlockFaceMixin}），从那里压 ThreadLocal。
 * 取不到时返回 {@code null}（paper 在那种路径下也没有朝向）。
 */
@Mixin(BlockDamageEvent.class)
public abstract class BlockDamageEventApiMixin {

    @Unique
    private BlockFace paperarc$blockFace;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/block/Block;Lorg/bukkit/inventory/ItemStack;Z)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureBlockFace(Player player, Block block, ItemStack itemInHand,
                                           boolean instaBreak, CallbackInfo ci) {
        this.paperarc$blockFace = PaperarcEventCauses.takeBlockDamageFace();
    }

    @Unique
    public BlockFace getBlockFace() {
        return this.paperarc$blockFace;
    }
}
