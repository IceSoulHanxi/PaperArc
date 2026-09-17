package com.ixnah.mc.paperarc.mixin.common.player;

import com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * PlayerAttackEntityCooldownResetEvent 触发点（1.20.1 Arclight Trials 版）。
 * <p>
 * 1.20.1 的 Arclight 在 {@code LivingEntity.damageEntity0} 私有方法里，
 * 伤害事件结算（CraftEventFactory.handleLivingEntityDamageEvent）之后执行
 * {@code ((Player) damagesource.getEntity()).resetAttackStrengthTicker()}
 * ——即攻击者成功造成伤害后重置攻击冷却，与 Paper 的触发语义一致。
 * 本 wrap 锚定该调用；取消 = 不调用 original（跳过重置），与 Paper 的
 * {@code if (...callEvent()) { reset } } 分支等价。
 * <p>
 * {@code damageEntity0} 是 Arclight @Unique 方法（注入 LivingEntity），编译期不存在
 * → 整个注解 {@code remap=false}、按字面名匹配。**代价是 @At 的 target 也不过 refmap**，
 * 因此被锚的 vanilla 方法必须写 srg 字面名（{@code Player.resetAttackStrengthTicker}
 * → {@code m_36334_}）。此前写 mojmap 名导致注入恒不命中（run 2026-09-16
 * {@code mixin.debug.countInjections} 实测 "Scanned 1 target(s)" 但 0 succeeded）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAttackCooldownResetMixin {

    @WrapOperation(
        method = "damageEntity0(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;m_36334_()V"
        )
    )
    private void paperarc$onCooldownReset(Player attacker, Operation<Void> original) {
        if (!(attacker instanceof ServerPlayer serverPlayer)) {
            original.call(attacker);
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        PlayerAttackEntityCooldownResetEvent event = new PlayerAttackEntityCooldownResetEvent(
            PaperArcBridge.bukkitPlayer(serverPlayer),
            PaperArcBridge.bukkitEntity(self),
            serverPlayer.getAttackStrengthScale(0.0F)
        );
        if (event.callEvent()) {
            original.call(attacker);
        }
    }
}
