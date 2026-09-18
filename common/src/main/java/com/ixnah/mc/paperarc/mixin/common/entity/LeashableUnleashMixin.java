package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.ItemLike;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code EntityUnleashEvent} 的取消与 {@code isDropLeash()} 的消费方。
 *
 * <p><b>1.21.1 把拴绳逻辑整体挪进了 {@code Leashable} 接口</b>（1.20.1 是
 * {@code Mob}/{@code PathfinderMob} 上的实例方法）。Arclight 的
 * {@code LeashableMixin#arclight$unleashEvent} 只 {@code @Decorate} 了
 * <b>{@code tickLeash} 里那一次</b> {@code dropLeash(E,Z,Z)} 调用点
 * （另有 {@code leashTooFarBehaviour} 的 HEAD 派发 DISTANCE 一条），
 * 事件在进入静态方法体之前就已派发 —— 所以在方法体里守住它做的三件事即可。
 *
 * <p>随之而来的口径：**插件直接调 {@code LivingEntity#setLeashHolder(null)} 不会有事件**
 * （CraftBukkit 的 {@code unleash()} 直接调 {@code Mob#dropLeash}，Arclight 没在那儿派发），
 * 这是 Arclight 既有行为，不是本次改动引入的。
 *
 * <ul>
 *   <li>{@code setLeashData(null)}：取消 → 不解绑（实体仍拴着）；</li>
 *   <li>{@code spawnAtLocation(Items.LEAD)}：取消或 {@code !isDropLeash()} → 不掉绳子；</li>
 *   <li>{@code broadcastAndSend(解绑包)}：取消 → 不广播（客户端不会看到绳子消失）。</li>
 * </ul>
 */
@Mixin(Leashable.class)
public interface LeashableUnleashMixin {

    /**
     * 每次 {@code tickLeash} 开头清一次"最近一次事件"：Arclight 只在
     * {@code tickLeash} 里那次 {@code dropLeash(E,Z,Z)} 调用点与
     * {@code leashTooFarBehaviour} 的 HEAD 派发 {@code EntityUnleashEvent}
     * （`javap` 核对它的 {@code @Decorate} 注解），这里是唯一稳定的"一轮开始"边界。
     */
    @Inject(method = "tickLeash", at = @At("HEAD"))
    private static void paperarc$resetUnleashEvent(Entity entity, CallbackInfo ci) {
        PaperarcEventCauses.popUnleashEvent();
    }

    /**
     * {@code isDropLeash()} 的默认值就是这次调用真实传进来的 {@code dropLead}。
     * 挂在公共 default 方法的 HEAD —— {@code leashTooFarBehaviour} 走的是这一条，
     * 且早于 Arclight 在静态重载上的 decorate，事件构造器一定取得到。
     * {@code tickLeash} 直接调静态重载、绕过这里，那一路的实参本来就是 {@code true}，
     * 与 {@code takeUnleashDropLeash()} 的默认值一致。
     */
    @Inject(method = "dropLeash(ZZ)V", at = @At("HEAD"))
    private void paperarc$captureDropLeash(boolean sendPacket, boolean dropLead, CallbackInfo ci) {
        PaperarcEventCauses.pushUnleashDropLeash(dropLead);
    }

    @WrapWithCondition(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Leashable;setLeashData(Lnet/minecraft/world/entity/Leashable$LeashData;)V"))
    private static boolean paperarc$applyUnleashCancel(Leashable leashable, Leashable.LeashData data) {
        EntityUnleashEvent event = PaperarcEventCauses.unleashEvent();
        return event == null || !event.isCancelled();
    }

    @WrapWithCondition(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private static boolean paperarc$applyDropLeash(Entity entity, ItemLike item) {
        EntityUnleashEvent event = PaperarcEventCauses.unleashEvent();
        return event == null || (!event.isCancelled() && event.isDropLeash());
    }

    @WrapWithCondition(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerChunkCache;broadcast(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/protocol/Packet;)V"))
    private static boolean paperarc$applyUnleashBroadcast(ServerChunkCache chunkSource, Entity entity,
                                                          net.minecraft.network.protocol.Packet<?> packet) {
        EntityUnleashEvent event = PaperarcEventCauses.unleashEvent();
        return event == null || !event.isCancelled();
    }
}
