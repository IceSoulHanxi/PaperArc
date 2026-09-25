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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's PhantomPreSpawnEvent.
 */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {

    private boolean paperarc$abortSpawn;

    @Inject(method = "tick", at = @At("HEAD"))
    private void paperarc$resetAbortFlag(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
        this.paperarc$abortSpawn = false;
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"
            )
    )
    private Entity paperarc$phantomPreSpawn(EntityType<?> phantomType, Level level, EntitySpawnReason reason, Operation<Entity> original,
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
        return original.call(phantomType, level, reason);
    }
}
