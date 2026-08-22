package dev.paperarc.mixin.common.block;

import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's BlockBreakProgressUpdateEvent
 * (Add-BlockBreakProgressUpdateEvent.patch).
 *
 * Paper fires the event inside ServerLevel#destroyBlockProgress right before
 * the packet broadcast loop, skipping it when no server entity matches the
 * breaker id (client-side-only ids used by plugins). We inject at HEAD —
 * vanilla has no early return before the broadcast loop, so HEAD is
 * equivalent to Paper's pre-loop injection point and avoids Arclight's
 * @Decorate on the List#iterator callsite.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelBlockBreakProgressMixin {

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    private void paperarc$blockBreakProgressUpdate(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        // Match Paper: resolve the breaking entity by entity id; skip fake ids.
        // PlayerList has no getPlayer(int) in 1.21.1, so use level#getEntity.
        Entity breaker = ((ServerLevel) (Object) this).getEntity(breakerId);
        if (breaker == null) {
            return;
        }
        float progressFloat = Mth.clamp(progress, 0, 10) / 10.0f;
        CraftBlock bukkitBlock = CraftBlock.at((ServerLevel) (Object) this, pos);
        new BlockBreakProgressUpdateEvent(bukkitBlock, progressFloat, PaperArcBridge.bukkitEntity(breaker))
                .callEvent();
    }
}
