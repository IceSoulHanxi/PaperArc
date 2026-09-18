package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * gaps.md E6：给实体记下生成原因（{@code Entity#getEntitySpawnReason()} /
 * {@code fromMobSpawner()}），原先两者恒返回默认值。
 *
 * <p>Paper 在 {@code ServerLevel#addFreshEntity(Entity, SpawnReason)} 里写；
 * Arclight 把原因压在 {@code ServerLevel} 的一个字段上（{@code bridge$pushAddEntityReason}），
 * 所有入口（刷怪笼、自然生成、命令、插件）最后都汇到 {@code addEntity(Entity)}，
 * 在它的 HEAD 读一次就全覆盖 —— 而且必须在 HEAD：Arclight 派发
 * {@code CreatureSpawnEvent} 之后就把那个字段清空了。
 *
 * <p>只在实体还没有原因时写（{@code if null}），与 Paper 一致：
 * 从存档读回来的实体已经带着 {@code Paper.SpawnReason}，不该被重新落地的动作覆盖。
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelSpawnReasonMixin {

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void paperarc$captureSpawnReason(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        EntityBridge bridge = (EntityBridge) entity;
        if (bridge.paper$spawnReason() != null) {
            return;
        }
        CreatureSpawnEvent.SpawnReason reason = ((WorldBridge) this).bridge$getAddEntityReason();
        if (reason == null) {
            return;
        }
        bridge.paper$setSpawnReason(reason);
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            bridge.paper$setSpawnedViaMobSpawner(true);
        }
    }
}
