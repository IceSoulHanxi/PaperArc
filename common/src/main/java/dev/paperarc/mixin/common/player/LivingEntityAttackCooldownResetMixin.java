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
 * 对照 Paper：LivingEntity#hurt 中伤害事件之后、原 resetAttackStrengthTicker()
 * 调用处，若攻击者为 ServerPlayer 则发事件，取消时不重置攻击冷却；
 * 非玩家/非 ServerPlayer 攻击者保持原行为直接重置。
 * <p>
 * 实现：wrap 该 INVOKE。取消 = 不调用 original（即跳过 resetAttackStrengthTicker），
 * 与 Paper 的 if (...callEvent()) { reset } 分支等价。
 * <p>
 * 冲突评估：Arclight 对 hurt 用 @Decorate 接管，但
 * resetAttackStrengthTicker 调用指令保留在其处理体中（core 变体在
 * arclight$fireEntityDamageEvent 内、vanilla 变体在 arclight$entityDamageEvent
 * 内各保留一处）；本 wrap 只针对方法体内该指令，不与 Decorate 竞争。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAttackCooldownResetMixin {

    @WrapOperation(
        method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"
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
