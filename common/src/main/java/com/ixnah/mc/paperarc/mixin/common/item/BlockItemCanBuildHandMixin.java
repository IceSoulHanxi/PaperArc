package com.ixnah.mc.paperarc.mixin.common.item;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code BlockCanBuildEvent#getHand()} 的取值来源之一。
 *
 * <p>Arclight 在 {@code canPlace} 里构造事件，构造点拿不到 {@code context}；
 * 在 HEAD 把手压进 ThreadLocal，由事件构造器取回。
 */
@Mixin(BlockItem.class)
public abstract class BlockItemCanBuildHandMixin {

    @Inject(method = "canPlace", at = @At("HEAD"))
    private void paperarc$captureCanBuildHand(BlockPlaceContext context, BlockState state,
                                              CallbackInfoReturnable<Boolean> cir) {
        PaperarcEventCauses.pushBlockCanBuildHand(CraftEquipmentSlot.getHand(context.getHand()));
    }
}
