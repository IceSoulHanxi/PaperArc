package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.ShieldBlockingDelayBridge;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Paper 的 Make-shield-blocking-delay-configurable：把 {@code isBlocking()} 里硬编码的
 * {@code 5} 换成可配置的每实体延迟，并把状态放在 NMS 侧。
 *
 * <p>默认值 5 与 vanilla 一致（Paper 取 {@code paperConfig().misc.shieldBlockingDelay}，
 * 其默认也是 5，Arclight 没有 paper 配置）。{@code isBlocking()} 里 {@code iconst_5}
 * 只出现一次（`javap -c` 核对），所以 {@code @ModifyConstant} 的锚点唯一。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldBlockingDelayMixin implements ShieldBlockingDelayBridge {

    @Unique
    public int shieldBlockingDelay = 5; // Paper

    @Override
    public int paperarc$getShieldBlockingDelay() {
        return this.shieldBlockingDelay;
    }

    @Override
    public void paperarc$setShieldBlockingDelay(int delay) {
        this.shieldBlockingDelay = delay;
    }

    @ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5))
    private int paperarc$shieldBlockingDelay(int original) {
        return this.shieldBlockingDelay;
    }
}
