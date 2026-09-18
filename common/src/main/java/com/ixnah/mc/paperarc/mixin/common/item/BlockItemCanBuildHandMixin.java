package com.ixnah.mc.paperarc.mixin.common.item;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
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
 * <p>Arclight 把 {@code canPlace} 整个 {@code @Overwrite} 掉并在里面构造事件，
 * 构造点拿不到 {@code context}；在 HEAD 把手压进 {@link EventCauseState}，
 * 由事件构造器取回。HEAD 在 Arclight 的覆写体里同样存在，不受覆写影响。
 */
@Mixin(BlockItem.class)
public abstract class BlockItemCanBuildHandMixin {

    @Inject(method = "canPlace", at = @At("HEAD"))
    private void paperarc$captureCanBuildHand(BlockPlaceContext context, BlockState state,
                                              CallbackInfoReturnable<Boolean> cir) {
        EventCauseState.setBlockCanBuildHand(CraftEquipmentSlot.getHand(context.getHand()));
    }
}
