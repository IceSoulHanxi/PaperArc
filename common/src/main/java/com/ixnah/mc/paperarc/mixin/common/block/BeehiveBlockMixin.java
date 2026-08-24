package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.block.PlayerShearBlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's PlayerShearBlockEvent for BeehiveBlock
 * (Add-PlayerShearBlockEvent.patch).
 *
 * Paper fires the event inside the shears branch of useItemOn, before the
 * shear sound, and replaces the fixed honeycomb drop with event.getDrops();
 * a cancelled event returns SKIP_DEFAULT_BLOCK_INTERACTION. NeoForge's
 * branch condition is canPerformAction(ItemAbilities.SHEARS_HARVEST), so we
 * replicate it at HEAD, fire the event, and swap the dropHoneycomb call for
 * a loop over the (mutable) event drops via ThreadLocal handoff.
 */
@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockMixin {

    @Shadow
    @Final
    public static IntegerProperty HONEY_LEVEL;

    private static final ThreadLocal<java.util.List<org.bukkit.inventory.ItemStack>> paperarc$shearDrops = new ThreadLocal<>();

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void paperarc$playerShearBlock(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit,
                                           CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (level.isClientSide() || player == null) {
            return;
        }
        if (state.getValue(HONEY_LEVEL) < 5 || !stack.is(net.minecraft.world.item.Items.SHEARS)) {
            return;
        }
        java.util.List<org.bukkit.inventory.ItemStack> drops = new java.util.ArrayList<>();
        drops.add(CraftItemStack.asCraftMirror(new ItemStack(Items.HONEYCOMB, 3)));
        PlayerShearBlockEvent event = new PlayerShearBlockEvent(
                PaperArcBridge.bukkitPlayer(player), CraftBlock.at(level, pos),
                CraftItemStack.asCraftMirror(stack), CraftEquipmentSlot.getHand(hand), drops);
        if (!event.callEvent()) {
            cir.setReturnValue(ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION);
            return;
        }
        paperarc$shearDrops.set(event.getDrops());
    }

    @WrapOperation(method = "useItemOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BeehiveBlock;dropHoneycomb(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void paperarc$dropEventDrops(Level level, BlockPos pos,
                                         Operation<Void> original) {
        java.util.List<org.bukkit.inventory.ItemStack> drops = paperarc$shearDrops.get();
        paperarc$shearDrops.remove();
        if (drops == null) {
            original.call(level, pos);
            return;
        }
        for (org.bukkit.inventory.ItemStack bukkitStack : drops) {
            ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);
            if (!nmsStack.isEmpty()) {
                BeehiveBlock.popResource(level, pos, nmsStack);
            }
        }
    }
}
