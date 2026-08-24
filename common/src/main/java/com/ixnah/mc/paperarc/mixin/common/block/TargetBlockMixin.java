package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.block.TargetHitEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's TargetHitEvent (Add-TargetHitEvent.patch).
 *
 * Paper fires the event in updateRedstoneOutput with the freshly computed
 * signal strength; a cancelled event keeps the original strength but skips
 * the redstone update AND the stat/advancement award in onProjectileHit;
 * otherwise event.getSignalStrength() replaces i for both the output power
 * and the return value. ThreadLocals carry the tri-state (cancelled /
 * override) from the HEAD inject to the setOutputPower wrapper, the return
 * value modifier and the award wrappers.
 */
@Mixin(TargetBlock.class)
public abstract class TargetBlockMixin {

    @Shadow
    private static int getRedstoneStrength(BlockHitResult hitResult, Vec3 location) {
        throw new AssertionError();
    }

    private static final ThreadLocal<Boolean> paperarc$skipAward = new ThreadLocal<>();
    private static final ThreadLocal<Integer> paperarc$signalOverride = new ThreadLocal<>();

    @Inject(method = "updateRedstoneOutput", at = @At("HEAD"), cancellable = true)
    private static void paperarc$targetHit(LevelAccessor level, BlockState state, BlockHitResult hitResult,
                                           Entity entity, CallbackInfoReturnable<Integer> cir) {
        paperarc$skipAward.set(Boolean.FALSE);
        paperarc$signalOverride.set(null);
        if (!(entity instanceof Projectile projectile)) {
            return;
        }
        int i = getRedstoneStrength(hitResult, hitResult.getLocation());
        TargetHitEvent event = new TargetHitEvent(
                (org.bukkit.entity.Projectile) PaperArcBridge.bukkitEntity(projectile),
                CraftBlock.at(level, hitResult.getBlockPos()),
                CraftBlock.notchToBlockFace(hitResult.getDirection()), i);
        if (!event.callEvent()) {
            paperarc$skipAward.set(Boolean.TRUE);
            cir.setReturnValue(i);
            return;
        }
        paperarc$signalOverride.set(event.getSignalStrength());
    }

    @WrapOperation(method = "updateRedstoneOutput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TargetBlock;setOutputPower(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;ILnet/minecraft/core/BlockPos;I)V"))
    private static void paperarc$overrideOutput(LevelAccessor level, BlockState state, int i, BlockPos pos, int j,
                                                Operation<Void> original) {
        Integer override = paperarc$signalOverride.get();
        original.call(level, state, override != null ? override : i, pos, j);
    }

    @ModifyReturnValue(method = "updateRedstoneOutput", at = @At("RETURN"))
    private static int paperarc$overrideReturn(int original) {
        Integer override = paperarc$signalOverride.get();
        paperarc$signalOverride.set(null);
        return override != null ? override : original;
    }

    @WrapOperation(method = "onProjectileHit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/resources/ResourceLocation;)V"))
    private void paperarc$skipAwardStat(net.minecraft.server.level.ServerPlayer player,
                                        net.minecraft.resources.ResourceLocation stat, Operation<Void> original) {
        if (Boolean.TRUE.equals(paperarc$skipAward.get())) {
            return;
        }
        original.call(player, stat);
    }

    @WrapOperation(method = "onProjectileHit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/TargetBlockTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;I)V"))
    private void paperarc$skipAwardTrigger(net.minecraft.advancements.critereon.TargetBlockTrigger trigger,
                                           net.minecraft.server.level.ServerPlayer player, Entity entity, Vec3 location, int i,
                                           Operation<Void> original) {
        boolean cancelled = Boolean.TRUE.equals(paperarc$skipAward.get());
        paperarc$skipAward.set(Boolean.FALSE);
        if (cancelled) {
            return;
        }
        original.call(trigger, player, entity, location, i);
    }
}
