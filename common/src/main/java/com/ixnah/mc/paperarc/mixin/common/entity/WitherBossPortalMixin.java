package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.WitherBossBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 {@code Wither#setCanTravelThroughPortals} 在 NMS 侧真的决定凋灵能否走传送门。
 *
 * <p>vanilla {@code WitherBoss#canUsePortal(boolean)} 恒返回 false；Paper 把它改成
 * {@code super.canUsePortal(b) && this.canPortal}（Missing-Entity-API）。mixin 里调不到 super，
 * 这里按 {@code LivingEntity}/{@code Entity} 的实现展开同一个条件。
 * 1.21.1 锚的是 {@code Entity#canChangeDimensions(Level, Level)}，1.21.11 已改名
 * {@code canTeleport}，且那样只能多拦、放不行（canUsePortal 先返回了 false）。</p>
 */
@Mixin(WitherBoss.class)
public abstract class WitherBossPortalMixin {

    @Inject(method = "canUsePortal", at = @At("HEAD"), cancellable = true)
    private void paperarc$witherPortalSwitch(boolean ignorePassenger, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        cir.setReturnValue(((WitherBossBridge) this).paper$canPortal()
                && (ignorePassenger || !self.isPassenger()) && self.isAlive() && !self.isSleeping());
    }
}
