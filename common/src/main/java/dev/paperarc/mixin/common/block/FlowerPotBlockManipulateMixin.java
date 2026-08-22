package dev.paperarc.mixin.common.block;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.CraftBlockType;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * PlayerFlowerPotManipulateEvent 触发点。
 * <p>
 * 对照 Paper：FlowerPotBlock#useItemOn（种花）在 setBlock 前发事件
 * (placing=true, item=手持物品副本)，取消时同步容器并返回 CONSUME；
 * FlowerPotBlock#useWithoutItem（取花）在 addItem 前发事件
 * (placing=false, item=盆内植物 ItemStack)，取消时同步容器并返回 PASS。
 * <p>
 * 实现：cancellable @Inject 于 useItemOn 的 Level#setBlock INVOKE 前
 * （该方法内唯一 setBlock 调用，到达即代表所有原版前置检查已通过），
 * 以及 useWithoutItem 的 Player#addItem INVOKE 前。
 * <p>
 * 偏差：Paper 在双端通用代码里触发事件；这里加 isClientSide 守卫，
 * 仅服务端触发（Arclight 为服务端环境，避免客户端误发 Bukkit 事件）。
 */
@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockManipulateMixin {

    @Inject(
        method = "useItemOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
        ),
        cancellable = true
    )
    private void paperarc$onPlantFlower(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                        InteractionHand hand, BlockHitResult hit,
                                        CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (level.isClientSide) {
            return;
        }
        PlayerFlowerPotManipulateEvent event = new PlayerFlowerPotManipulateEvent(
            PaperArcBridge.bukkitPlayer(player),
            CraftBlock.at(level, pos),
            CraftItemStack.asBukkitCopy(stack),
            true
        );
        if (!event.callEvent()) {
            player.containerMenu.sendAllDataToRemote(); // 同步客户端
            cir.setReturnValue(ItemInteractionResult.CONSUME);
        }
    }

    @Inject(
        method = "useWithoutItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;addItem(Lnet/minecraft/world/item/ItemStack;)Z"
        ),
        cancellable = true
    )
    private void paperarc$onTakeFlower(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide) {
            return;
        }
        PlayerFlowerPotManipulateEvent event = new PlayerFlowerPotManipulateEvent(
            PaperArcBridge.bukkitPlayer(player),
            CraftBlock.at(level, pos),
            new org.bukkit.inventory.ItemStack(
                CraftBlockType.minecraftToBukkit(((FlowerPotBlock) (Object) this).getPotted())),
            false
        );
        if (!event.callEvent()) {
            player.containerMenu.sendAllDataToRemote(); // 同步客户端
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
