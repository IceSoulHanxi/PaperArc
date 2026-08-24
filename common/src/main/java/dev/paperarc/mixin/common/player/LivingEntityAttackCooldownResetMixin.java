package dev.paperarc.mixin.common.player;

import com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * PlayerAttackEntityCooldownResetEvent 触发点。
 * <p>
 * 对照 Paper：LivingEntity#hurt 中冷却拒绝分支（damage &lt;= lastHurt）里
 * 原 resetAttackStrengthTicker() 调用处，若攻击者为 ServerPlayer 则发事件，
 * 取消时不重置攻击冷却；非玩家/非 ServerPlayer 攻击者保持原行为直接重置。
 * <p>
 * 实现说明：vanilla hurt 体内并无该调用——Arclight 把伤害事件逻辑放进
 * 自己的处理方法 arclight$fireEntityDamageEvent（core LivingEntityMixin，
 * 签名 (DamageSource;F)EntityDamageEvent），resetAttackStrengthTicker 调用
 * 位于其冷却拒绝分支内。本 wrap 锚定该合并后的处理方法；取消 = 不调用
 * original（跳过 reset），与 Paper 的 if (...callEvent()) { reset } 分支等价。
 * require = 0 保证 Arclight 上游重构时静默降级而非启动崩。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAttackCooldownResetMixin {

    @WrapOperation(
        method = "arclight$fireEntityDamageEvent(Lnet/minecraft/world/damagesource/DamageSource;F)Lorg/bukkit/event/entity/EntityDamageEvent;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V",
            remap = false
        ),
        require = 0
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
