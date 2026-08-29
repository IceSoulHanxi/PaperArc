package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Port of Paper's EntityPathfindEvent (EntityPathfindEvent.patch).
 *
 * <p>Paper threads an optional target entity through new createPath overloads and filters
 * cancelled positions inside {@code createPath(Set, int, int, int, float)}. Under Arclight only
 * the vanilla overload exists at runtime, so we inject its HEAD: every candidate position fires
 * one event; cancelled positions are filtered out and the path is re-created from the remaining
 * set via a recursion guarded by a ThreadLocal (so events are not fired twice). If all positions
 * are cancelled no path is created.
 *
 * <p>Deviation vs Paper: the event's {@code targetEntity} is always {@code null} because the
 * runtime overload does not carry the target entity.
 */
@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {

    @Accessor("mob")
    abstract Mob paperarc$getMob();

    @Invoker("createPath")
    abstract Path paperarc$createPath(Set<BlockPos> positions, int range, boolean useHeadPos, int distance, float followRange);

    private static final ThreadLocal<Boolean> paperarc$repathing = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(
            method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void paperarc$pathfindEvent(Set<BlockPos> positions, int range, boolean useHeadPos, int distance,
                                        float followRange, CallbackInfoReturnable<Path> cir) {
        if (paperarc$repathing.get()) {
            return; // inner call issued by us below — do not fire events again
        }
        if (!(paperarc$getMob().level() instanceof ServerLevel serverLevel) || positions.isEmpty()) {
            return;
        }
        Set<BlockPos> allowed = new HashSet<>();
        boolean changed = false;
        for (BlockPos pos : positions) {
            Location loc = new Location(PaperArcBridge.bukkitWorld(serverLevel), pos.getX(), pos.getY(), pos.getZ());
            EntityPathfindEvent event = new EntityPathfindEvent(
                    PaperArcBridge.bukkitEntity(this.paperarc$getMob()), loc, null);
            if (event.callEvent()) {
                allowed.add(pos);
            } else {
                changed = true;
            }
        }
        if (!changed) {
            return;
        }
        if (allowed.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }
        paperarc$repathing.set(Boolean.TRUE);
        try {
            cir.setReturnValue(this.paperarc$createPath(allowed, range, useHeadPos, distance, followRange));
        } finally {
            paperarc$repathing.set(Boolean.FALSE);
        }
    }
}
