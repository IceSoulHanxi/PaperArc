package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * PlayerFlowerPotManipulateEvent 触发点（1.20.1 单 use 方法版）。
 * <p>
 * 1.20.1 的 FlowerPotBlock#use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)
 * 内：盆空时 setBlock(offset 90, placing=true)，盆有花时 setBlock(offset 183, placing=false)。
 * 参照 Paper 1.20.1 补丁：在 setBlock 前发事件，取消时同步客户端并返回 PASS。
 */
@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockManipulateMixin {

    @Inject(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            ordinal = 0
        ),
        cancellable = true
    )
    private void paperarc$onPlantFlower(BlockState state, Level level, BlockPos pos, Player player,
                                        InteractionHand hand, BlockHitResult hit,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide) {
            return;
        }
        // placing=true：盆空，player 手上拿的是要种的花
        PlayerFlowerPotManipulateEvent event = new PlayerFlowerPotManipulateEvent(
            PaperArcBridge.bukkitPlayer(player),
            CraftBlock.at(level, pos),
            CraftItemStack.asBukkitCopy(player.getItemInHand(hand)),
            true
        );
        if (!event.callEvent()) {
            PaperArcBridge.bukkitPlayer(player).sendBlockChange(
                CraftBlock.at(level, pos).getLocation(), CraftBlock.at(level, pos).getBlockData());
            PaperArcBridge.bukkitPlayer(player).updateInventory();
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            ordinal = 1
        ),
        cancellable = true
    )
    private void paperarc$onTakeFlower(BlockState state, Level level, BlockPos pos, Player player,
                                       InteractionHand hand, BlockHitResult hit,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide) {
            return;
        }
        // placing=false：盆有花，玩家取走盆内植物
        org.bukkit.Material pottedMaterial =
            CraftMagicNumbers.getMaterial(((FlowerPotBlock) (Object) this).getContent());
        PlayerFlowerPotManipulateEvent event = new PlayerFlowerPotManipulateEvent(
            PaperArcBridge.bukkitPlayer(player),
            CraftBlock.at(level, pos),
            new org.bukkit.inventory.ItemStack(pottedMaterial, 1),
            false
        );
        if (!event.callEvent()) {
            PaperArcBridge.bukkitPlayer(player).sendBlockChange(
                CraftBlock.at(level, pos).getLocation(), CraftBlock.at(level, pos).getBlockData());
            PaperArcBridge.bukkitPlayer(player).updateInventory();
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
