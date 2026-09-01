package com.ixnah.mc.paperarc.mixin.common.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.papermc.paper.event.block.BellRevealRaiderEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's Add-BellRevealRaiderEvent.patch.
 *
 * <p>In this target {@code glow(LivingEntity)} is reached through a method
 * reference (invokedynamic) inside {@code makeRaidersGlow}, so the event fires
 * from a HEAD injection on {@code glow} itself. The bell position/world needed
 * by the event is carried from {@code makeRaidersGlow} via thread-locals
 * ({@code glow} has no world/pos parameters), and cleared on return.
 * Cancellation skips the glowing effect entirely, as in Paper.
 */
@Mixin(BellBlockEntity.class)
public abstract class BellBlockEntityMixin {

    @Unique
    private static final ThreadLocal<Level> paperarc$glowWorld = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<BlockPos> paperarc$glowPos = new ThreadLocal<>();

    @Inject(method = "makeRaidersGlow", at = @At("HEAD"))
    private static void paperarc$captureGlowContext(Level world, BlockPos pos, java.util.List<LivingEntity> entities, CallbackInfo ci) {
        paperarc$glowWorld.set(world);
        paperarc$glowPos.set(pos);
    }

    @Inject(method = "makeRaidersGlow", at = @At("RETURN"))
    private static void paperarc$clearGlowContext(Level world, BlockPos pos, java.util.List<LivingEntity> entities, CallbackInfo ci) {
        paperarc$glowWorld.remove();
        paperarc$glowPos.remove();
    }

    @Inject(method = "glow(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void paperarc$fireRevealRaider(LivingEntity entity, CallbackInfo ci) {
        Level world = paperarc$glowWorld.get();
        BlockPos pos = paperarc$glowPos.get();
        if (world == null || pos == null) {
            return; // glow invoked outside makeRaidersGlow: no event, vanilla behaviour
        }
        BellRevealRaiderEvent event = new BellRevealRaiderEvent(
                CraftBlock.at(world, pos),
                (org.bukkit.entity.Raider) PaperArcBridge.bukkitEntity(entity));
        if (!event.callEvent()) {
            ci.cancel();
        }
    }
}
