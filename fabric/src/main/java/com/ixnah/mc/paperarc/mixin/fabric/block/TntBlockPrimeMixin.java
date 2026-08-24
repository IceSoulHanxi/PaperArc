package com.ixnah.mc.paperarc.mixin.fabric.block;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's TNTPrimeEvent for TntBlock (Add-TNTPrimeEvent.patch).
 *
 * Runtime server is VANILLA 1.21.1 (fabric intermediary): every priming path
 * funnels through the static {@code explode} calls inside TntBlock itself
 * ({@code explode(Level,Pos)} for redstone paths, private
 * {@code explode(Level,Pos,LivingEntity)} for item/projectile paths), NOT
 * through the NeoForge-style {@code onCaughtFire} hook present in the
 * compile-time merged jar. Each Paper site maps to an injection right before
 * the matching INVOKE with the matching PrimeReason; cancelling skips priming
 * and the follow-up block removal, exactly like Paper's early return.
 *
 * Conflict notes vs Arclight: core TntBlockMixin redirects hasNeighborSignal
 * in onPlace/neighborChanged (different At point) and TntBlockMixin_Vanilla
 * targets the same explode sites with require = 0 (no-op on collision).
 */
@Mixin(TntBlock.class)
public abstract class TntBlockPrimeMixin {

    @Inject(method = "onPlace", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"),
            cancellable = true)
    private void paperarc$primeRedstoneOnPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
                                               boolean notify, CallbackInfo ci) {
        if (!new TNTPrimeEvent(CraftBlock.at(level, pos), TNTPrimeEvent.PrimeReason.REDSTONE, null).callEvent()) {
            ci.cancel();
        }
    }

    @Inject(method = "neighborChanged", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"),
            cancellable = true)
    private void paperarc$primeRedstoneNeighbor(BlockState state, Level level, BlockPos pos,
                                                net.minecraft.world.level.block.Block sourceBlock, BlockPos sourcePos,
                                                boolean notify, CallbackInfo ci) {
        if (!new TNTPrimeEvent(CraftBlock.at(level, pos), TNTPrimeEvent.PrimeReason.REDSTONE, null).callEvent()) {
            ci.cancel();
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V"),
            cancellable = true)
    private void paperarc$primeItem(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                    InteractionHand hand, BlockHitResult hit,
                                    CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!new TNTPrimeEvent(CraftBlock.at(level, pos), TNTPrimeEvent.PrimeReason.ITEM,
                PaperArcBridge.bukkitPlayer(player)).callEvent()) {
            cir.setReturnValue(ItemInteractionResult.FAIL);
        }
    }

    @Inject(method = "onProjectileHit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V"),
            cancellable = true)
    private void paperarc$primeProjectile(Level level, BlockState state, BlockHitResult hit, Projectile projectile,
                                          CallbackInfo ci) {
        if (!new TNTPrimeEvent(CraftBlock.at(level, hit.getBlockPos()), TNTPrimeEvent.PrimeReason.PROJECTILE,
                PaperArcBridge.bukkitEntity(projectile)).callEvent()) {
            ci.cancel();
        }
    }

    @Inject(method = "wasExploded", at = @At("HEAD"), cancellable = true)
    private void paperarc$primeExplosion(Level level, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        org.bukkit.entity.Entity source = explosion.getDirectSourceEntity() != null ? PaperArcBridge.bukkitEntity(explosion.getDirectSourceEntity()) : null;
        if (!new TNTPrimeEvent(CraftBlock.at(level, pos), TNTPrimeEvent.PrimeReason.EXPLOSION, source).callEvent()) {
            ci.cancel();
        }
    }
}
