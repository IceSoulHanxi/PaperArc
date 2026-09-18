package com.ixnah.mc.paperarc.mixin.common.item;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code BlockCanBuildEvent#getHand()} 的另一个取值来源。
 *
 * <p>Arclight 在 {@code getPlacementState} 的 RETURN 处才构造事件，那时内层
 * {@code BlockItem#canPlace} 早已返回；所以这一路要在本方法 HEAD 再压一次
 * （两处压的是同一个 {@code context.getHand()}，互相覆盖无害）。
 */
@Mixin(StandingAndWallBlockItem.class)
public abstract class StandingAndWallBlockItemCanBuildHandMixin {

    @Inject(method = "getPlacementState", at = @At("HEAD"))
    private void paperarc$captureCanBuildHand(BlockPlaceContext context,
                                              CallbackInfoReturnable<BlockState> cir) {
        EventCauseState.setBlockCanBuildHand(CraftEquipmentSlot.getHand(context.getHand()));
    }
}
