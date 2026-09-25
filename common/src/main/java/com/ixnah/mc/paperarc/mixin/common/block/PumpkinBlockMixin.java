package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.block.PlayerShearBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Port of Paper's PlayerShearBlockEvent for PumpkinBlock
 * (Add-PlayerShearBlockEvent.patch).
 *
 * Paper fires the event in the server-side carve branch of useItemOn, before
 * the carve sound; a cancelled event returns SKIP_DEFAULT_BLOCK_INTERACTION
 * and the carved seeds come from event.getDrops() instead of the fixed
 * pumpkin-seeds stack.
 */
@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockMixin {

    private static final ThreadLocal<List<org.bukkit.inventory.ItemStack>> paperarc$shearDrops = new ThreadLocal<>();

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void paperarc$playerShearBlock(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit,
                                           CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide() || player == null) {
            return;
        }
        if (!stack.is(net.minecraft.world.item.Items.SHEARS)) {
            return;
        }
        List<org.bukkit.inventory.ItemStack> drops = new java.util.ArrayList<>();
        drops.add(CraftItemStack.asCraftMirror(new ItemStack(Items.PUMPKIN_SEEDS, 4)));
        PlayerShearBlockEvent event = new PlayerShearBlockEvent(
                PaperArcBridge.bukkitPlayer(player), CraftBlock.at(level, pos),
                CraftItemStack.asCraftMirror(stack), CraftEquipmentSlot.getHand(hand), drops);
        if (!event.callEvent()) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }
        paperarc$shearDrops.set(event.getDrops());
    }

    @WrapOperation(method = "useItemOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/PumpkinBlock;dropFromBlockInteractLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Ljava/util/function/BiConsumer;)Z"))
    private boolean paperarc$dropEventDrops(ServerLevel level,
                                            ResourceKey<LootTable> lootTable,
                                            BlockState state,
                                            BlockEntity blockEntity,
                                            ItemStack tool,
                                            Entity entity,
                                            BiConsumer<ServerLevel, ItemStack> dropConsumer,
                                            Operation<Boolean> original) {
        List<org.bukkit.inventory.ItemStack> drops = paperarc$shearDrops.get();
        paperarc$shearDrops.remove();
        if (drops == null) {
            return original.call(level, lootTable, state, blockEntity, tool, entity, dropConsumer);
        }
        for (org.bukkit.inventory.ItemStack bukkitStack : drops) {
            ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);
            if (!nmsStack.isEmpty()) {
                dropConsumer.accept(level, nmsStack);
            }
        }
        return true;
    }
}
