package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PotionEffectHiddenBridge;
import net.minecraft.world.effect.MobEffectInstance;
import org.bukkit.craftbukkit.v.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把 NMS 的 {@code MobEffectInstance.hiddenEffect} 链一并转成 Bukkit 的
 * {@code PotionEffect#getHiddenPotionEffect()}（Paper 同）。
 * {@code toBukkit} 是 CraftBukkit 转换的唯一入口，递归调用自己即可逐层填。
 */
@Mixin(CraftPotionUtil.class)
public abstract class CraftPotionUtilHiddenMixin {

    // 必须写全描述符：只写名字会撞上 toBukkit(PotionType) 那个重载（真机 InvalidInjectionException）。
    // 形参里有 NMS 类型，注解处理器能生成 refmap 条目，所以这里**不能**写 remap = false。
    @Inject(method = "toBukkit(Lnet/minecraft/world/effect/MobEffectInstance;)Lorg/bukkit/potion/PotionEffect;",
            at = @At("RETURN"))
    private static void paperarc$carryHiddenEffect(MobEffectInstance instance,
                                                   CallbackInfoReturnable<PotionEffect> cir) {
        PotionEffect result = cir.getReturnValue();
        if (result == null || instance == null || instance.hiddenEffect == null) {
            return;
        }
        ((PotionEffectHiddenBridge) result).paperarc$setHiddenPotionEffect(
                CraftPotionUtil.toBukkit(instance.hiddenEffect));
    }
}
