package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.DeathEventBridge;
import com.ixnah.mc.paperarc.bridge.DeathEventSupport;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper 的 Improve-death-events 在 NMS 侧的那一半：把死亡音效从 {@code hurt} 挪到事件之后，
 * 并让 {@code EntityDeathEvent} 的取消真的能让实体活过来。
 *
 * <p><b>两个锚点都在 Arclight {@code @Overwrite} 之后的 {@code hurt} 体里</b>
 * （Arclight 的 config 是 {@code mixinPriority: 500}、我们默认 1000，天然排在其后，
 * 见任务书 Y-1）。{@code playSound(SoundEvent,FF)} 与 {@code die} 在该方法体内各只出现一次
 * （`javap -c` 核对 Arclight 编译产物 {@code LivingEntityMixin#hurt}）。
 *
 * <p>{@code @Local(index = 6)} 是 {@code hurt} 里的 {@code flag1}
 * （Arclight 编译产物的 LocalVariableTable：slot 4 = flag、slot 6 = flag1、
 * slot 8 = flag2 且在 {@code die} 之后才进入作用域）。Paper 的语义就是
 * {@code silentDeath = !flag1}。
 *
 * <p><b>与 Paper 的偏差</b>：取消时我们在 {@code dropAllDeathLoot} 返回处
 * {@code ci.cancel()}，于是 {@code createWitherRose} / {@code broadcastEntityEvent(3)} /
 * {@code setPose(DYING)} 一并跳过 —— Paper 保留了 createWitherRose 调用，但它内部
 * 头一句就是 {@code if (this.dead && …)}，取消后同样什么都不做，等价。
 * 盔甲架走的是 {@code ArmorStand#kill()} 而不是 {@code die()}，取消对它无效（见 gaps.md）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathEventMixin {

    @Shadow
    protected boolean dead;

    @WrapWithCondition(
            method = "hurt",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private boolean paperarc$deferDeathSound(LivingEntity self, SoundEvent sound, float volume, float pitch) {
        // Paper 把这一句删掉了，改由 EntityDeathEvent 触发之后按事件里的值播
        return false;
    }

    @WrapOperation(
            method = "hurt",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void paperarc$markSilentDeath(LivingEntity self, DamageSource source, Operation<Void> original,
                                          @Local(index = 6) boolean flag1) {
        DeathEventBridge bridge = (DeathEventBridge) self;
        bridge.paperarc$setSilentDeath(!flag1);
        try {
            original.call(self, source);
        } finally {
            bridge.paperarc$setSilentDeath(false);
        }
    }

    @Inject(
            method = "die",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/entity/LivingEntity;dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V"),
            cancellable = true)
    private void paperarc$consumeDeathEvent(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        DeathEventBridge bridge = (DeathEventBridge) self;
        EntityDeathEvent event = bridge.paperarc$getPendingDeathEvent();
        bridge.paperarc$setPendingDeathEvent(null);
        if (event == null) {
            return;
        }
        if (event.isCancelled()) {
            this.dead = false;
            self.setHealth((float) event.getReviveHealth());
            ci.cancel();
        } else {
            DeathEventSupport.playDeathSound(self, event);
        }
    }
}
