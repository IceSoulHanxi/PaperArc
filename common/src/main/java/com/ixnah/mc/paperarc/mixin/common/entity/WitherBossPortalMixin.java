package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.WitherBossBridge;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 {@code Wither#setCanTravelThroughPortals(false)} 在 NMS 侧真的拦住传送门。
 *
 * <p>此前只有 API 侧字段读写（{@code WitherBossFieldsMixin.canPortal}），实体照样能走
 * 传送门。Paper 的做法是在 {@code WitherBoss} 上覆写 {@code canChangeDimensions}；
 * 1.20.1 的 vanilla {@code WitherBoss} 自己就有这个无参覆写（javap 核对，1.21.1 没有，
 * 所以 main 的 {@code 075d8c0} 只能注在 {@code Entity} 上），这里直接注在 HEAD。</p>
 */
@Mixin(WitherBoss.class)
public abstract class WitherBossPortalMixin {

    @Inject(method = "canChangeDimensions", at = @At("HEAD"), cancellable = true)
    private void paperarc$witherPortalSwitch(CallbackInfoReturnable<Boolean> cir) {
        if (!((WitherBossBridge) this).paper$canPortal()) {
            cir.setReturnValue(false);
        }
    }
}
