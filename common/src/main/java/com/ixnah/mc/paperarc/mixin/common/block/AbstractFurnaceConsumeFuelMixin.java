package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code FurnaceBurnEvent#willConsumeFuel()} 的消费方。
 *
 * <p>{@code serverTick} 里点火那一支只有一次 {@code ItemStack#shrink(1)}
 * （`javap -c` 核对 1.21.1：offset 212，紧跟着才是"空了就换成剩余物"），
 * 就是 paper 用 {@code willConsumeFuel()} 守住的那句。不消耗时燃料仍非空，
 * 后面那句自然也不会执行，与 paper 守整个分支等价。
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceConsumeFuelMixin {

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void paperarc$resetBurnEvent(ServerLevel level, BlockPos pos, BlockState state,
                                                AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
        PaperarcEventCauses.popFurnaceBurnEvent();
    }

    @WrapWithCondition(method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static boolean paperarc$consumeFuel(ItemStack fuel, int count) {
        FurnaceBurnEvent event = PaperarcEventCauses.takeFurnaceBurnEvent();
        return event == null || event.willConsumeFuel();
    }
}
