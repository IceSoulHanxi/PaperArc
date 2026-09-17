package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.WitherBossBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 {@code Wither#setCanTravelThroughPortals(false)} 在 NMS 侧真的拦住传送门。
 *
 * <p>Paper 的做法是在 {@code WitherBoss} 上覆写 {@code canChangeDimensions}；1.21.1 的
 * vanilla {@code WitherBoss} 根本没有这个覆写（方法只在 {@code Entity} 上），所以改成在
 * {@code Entity.canChangeDimensions(Level, Level)} 的 HEAD 注入、只对凋灵生效。
 * 之前（B2-2 之前）只有 API 侧字段读写，实体照样能走传送门。</p>
 */
@Mixin(Entity.class)
public abstract class WitherBossPortalMixin {

    @Inject(method = "canChangeDimensions", at = @At("HEAD"), cancellable = true)
    private void paperarc$witherPortalSwitch(Level from, Level to, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof WitherBoss
                && !((WitherBossBridge) this).paper$canPortal()) {
            cir.setReturnValue(false);
        }
    }
}
