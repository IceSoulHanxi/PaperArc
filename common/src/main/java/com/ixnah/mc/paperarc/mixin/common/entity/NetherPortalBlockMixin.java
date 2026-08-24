package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.DimensionTransition;
import org.bukkit.PortalType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's EntityPortalReadyEvent (Add-EntityPortalReadyEvent.patch).
 *
 * <p>Fires at the HEAD of {@code NetherPortalBlock#getPortalDestination}: when cancelled the
 * entity's portal process is cleared and no destination is produced (matching Paper). Plugins may
 * observe the default target world.
 *
 * <p>Deviation vs Paper: {@code setTargetWorld(...)} changes are NOT applied — re-routing the
 * destination would require reimplementing Paper's patched exit-portal flow. Only cancellation
 * semantics are honoured.
 */
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {

    @Inject(
            method = "getPortalDestination(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/portal/DimensionTransition;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void paperarc$portalReady(ServerLevel level, Entity entity, net.minecraft.core.BlockPos pos,
                                      CallbackInfoReturnable<DimensionTransition> cir) {
        // getTypeKey() is CraftBukkit-injected and absent at compile time; compare the dimension
        // resource key instead (covers all vanilla dimensions).
        ResourceKey<Level> key = level.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
        ServerLevel target = level.getServer().getLevel(key);
        EntityPortalReadyEvent event = new EntityPortalReadyEvent(
                PaperArcBridge.bukkitEntity(entity),
                target == null ? null : PaperArcBridge.bukkitWorld(target),
                PortalType.NETHER);
        if (!event.callEvent()) {
            entity.portalProcess = null;
            cir.setReturnValue(null);
        }
    }
}
