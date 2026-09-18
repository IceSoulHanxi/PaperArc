package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.EntityBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code Entity#fromMobSpawner()} / {@code getEntitySpawnReason()} 的取值来源（gaps.md E6）。
 *
 * <p>Paper 是在 {@code ServerLevel#addFreshEntity(Entity, SpawnReason)} 里直接写字段；
 * Arclight 也有那个重载，但**不能锚它** —— 它只是众多入口之一
 * （{@code addWithUUID}/{@code addDuringTeleport}/{@code tryAddFreshEntityWithPassengers}…
 * 各自把原因压进 {@code ServerLevel} 的一个字段），而且 Arclight 在派发
 * {@code CreatureSpawnEvent} 之后立刻把那个字段清掉。唯一的汇聚点是私有的
 * {@code addEntity(Entity)}，在它的 HEAD 读 Arclight 的 {@code bridge$getAddEntityReason()}
 * 才既全覆盖又还没被清。
 *
 * <p>{@code bridge$getAddEntityReason()} 是 Arclight 自加的普通方法（不是注入器 handler，
 * 名字稳定），按约定 {@code @Shadow} 它时写 {@code remap = false}。
 *
 * <p>{@code spawnedViaMobSpawner} 不另找锚点：刷怪笼那条路径 Arclight 压的就是
 * {@code SpawnReason.SPAWNER}，与 Paper 在 {@code BaseSpawner} 里置位等价。
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelSpawnReasonMixin {

    @Shadow(remap = false)
    public abstract CreatureSpawnEvent.SpawnReason bridge$getAddEntityReason();

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void paperarc$captureSpawnReason(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        CreatureSpawnEvent.SpawnReason reason = this.bridge$getAddEntityReason();
        if (reason == null) {
            return;
        }
        EntityBridge bridge = (EntityBridge) entity;
        bridge.paper$setSpawnReason(reason);
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            bridge.paper$setSpawnedViaMobSpawner(true);
        }
    }
}
