package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's PhantomPreSpawnEvent.
 * Paper fires the event inside the per-phantom loop of
 * {@code PhantomSpawner.tick} right before {@code EntityType.PHANTOM.create};
 * a cancellation with {@code shouldAbortSpawn()} breaks out of the loop,
 * otherwise it continues to the next phantom. We wrap the single
 * {@code EntityType.create(Level)} call: firing the event there and returning
 * {@code null} reproduces "continue" (vanilla null-check skips the phantom).
 * For "abort", we latch a flag so every subsequent create in this tick is
 * skipped as well (the remaining loop iterations only spawn phantoms, so this
 * is equivalent to Paper's break).
 * <p>
 * Note: Paper also stores {@code spawningEntity} on the NMS Phantom for
 * {@code CraftPhantom#getSpawningEntity}; that getter lives in CraftBukkit
 * (generated build), not mixinable here, so we do not track it.
 */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {

    private boolean paperarc$abortSpawn;

    @Inject(method = "tick", at = @At("HEAD"))
    private void paperarc$resetAbortFlag(ServerLevel level, boolean spawnPhantoms, boolean spawnFriends,
                                         CallbackInfoReturnable<Integer> cir) {
        this.paperarc$abortSpawn = false;
    }

    /**
     * {@code @Local(ordinal = 1) BlockPos} 是必须的：注入点处有两个 BlockPos 局部
     *（玩家所在方块与偏移后的生成坐标），隐式 @Local 无法消歧，MixinExtras 会静默产出
     * **零**注入 —— 此前一直藏在 require = 0 后面（与 1.20.1 A7 同一个坑）。ordinal 1 是生成坐标。
     */
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"
            )
    )
    private Entity paperarc$phantomPreSpawn(EntityType<?> phantomType, Level level, Operation<Entity> original,
                                            @Local ServerPlayer player, @Local(ordinal = 1) BlockPos spawnPos) {
        if (this.paperarc$abortSpawn) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        Location location = new Location(PaperArcBridge.bukkitWorld(serverLevel),
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        PhantomPreSpawnEvent event = new PhantomPreSpawnEvent(location,
                PaperArcBridge.bukkitPlayer(player), CreatureSpawnEvent.SpawnReason.NATURAL);
        if (!event.callEvent()) {
            if (event.shouldAbortSpawn()) {
                this.paperarc$abortSpawn = true;
            }
            return null;
        }
        return original.call(phantomType, level);
    }
}
