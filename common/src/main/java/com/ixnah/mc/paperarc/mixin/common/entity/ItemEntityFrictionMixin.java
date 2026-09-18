package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EntityBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * paper 的 Friction-API 在掉落物侧的消费方：{@code frictionState == FALSE} 时
 * 把每 tick 的水平摩擦系数按 paper 改成 {@code 1F}（完全不减速）。
 *
 * <p>{@code index = 4} 是 {@code ItemEntity#tick} 里那个摩擦系数局部变量的槽号
 * （官方名映射后的 forge jar 的 LocalVariableTable：slot 4 = {@code f1}，
 * 落地与不落地两个赋值点都写它）。1.20.1 单加载器，写死 index 安全（checklist bj）。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityFrictionMixin {

    @ModifyVariable(method = "tick", at = @At("STORE"), index = 4)
    private float paperarc$applyFrictionState(float friction) {
        return ((EntityBridge) this).paper$frictionState() == TriState.FALSE ? 1.0F : friction;
    }
}
