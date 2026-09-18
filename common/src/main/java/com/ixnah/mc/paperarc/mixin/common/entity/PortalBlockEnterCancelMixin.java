package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 兑现 {@code EntityPortalEnterEvent} 的取消（paper {@code Improve-PortalEvents.patch}）。
 *
 * <p>锚点要**在 Arclight fire 事件之后**：它在 {@code NetherPortalBlock} 是
 * {@code INVOKE Entity#setAsInsidePortal} 之前、在 {@code EndPortalBlock} 是更早的
 * {@code FIELD Level#isClientSide}。两边都取 {@code setAsInsidePortal} 这个调用点 ——
 * 对 {@code NetherPortalBlock} 是同一个节点（我们的配置优先级 1100 > Arclight 的 500，
 * 回调排在它之后），对 {@code EndPortalBlock} 是严格更靠后的位置。
 * {@code ci.cancel()} 之后 {@code setAsInsidePortal} 不会执行，实体不算进入传送门。
 *
 * <p>第一版锚在 HEAD，探针 P30 当场逼出：那时事件还没 fire，取消读不到，掉落物照样被送进下界。
 *
 * <p>末地折跃门（{@code EndGatewayBlock}）Arclight 不发这个事件，这里也不管。
 */
@Mixin({NetherPortalBlock.class, EndPortalBlock.class})
public abstract class PortalBlockEnterCancelMixin {

    @Inject(method = "entityInside",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setAsInsidePortal(Lnet/minecraft/world/level/block/Portal;Lnet/minecraft/core/BlockPos;)V"),
            cancellable = true)
    private void paperarc$consumePortalEnterCancel(BlockState state, Level level, BlockPos pos, Entity entity,
                                                   CallbackInfo ci) {
        EntityPortalEnterEvent event = PaperarcEventCauses.takePortalEnterEvent();
        if (event != null && event.isCancelled()) {
            ci.cancel();
        }
    }
}
