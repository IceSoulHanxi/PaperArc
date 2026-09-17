package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code LivingEntity} supplementary API fields
 * （upwardsMovement / hurtDirection / shieldBlockingDelay）。字段名对齐 Paper，
 * 默认值同 Paper：shieldBlockingDelay 取 {@code 5}（vanilla 的
 * {@code getShieldBlockDelay} 常量），其余为 0。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFieldsMixin implements LivingEntityFieldsBridge {

    @Unique
    public float upwardsMovement; // Paper

    @Unique
    public float hurtDirection; // Paper

    @Unique
    public int shieldBlockingDelay = 5; // Paper

    @Unique
    public net.kyori.adventure.util.TriState frictionState = net.kyori.adventure.util.TriState.NOT_SET; // Paper

    @Override
    public float paper$getUpwardsMovement() {
        return this.upwardsMovement;
    }

    @Override
    public void paper$setUpwardsMovement(float upwardsMovement) {
        this.upwardsMovement = upwardsMovement;
    }

    @Override
    public float paper$getHurtDirection() {
        return this.hurtDirection;
    }

    @Override
    public void paper$setHurtDirection(float hurtDirection) {
        this.hurtDirection = hurtDirection;
    }

    @Override
    public net.kyori.adventure.util.TriState paper$getFrictionState() {
        return this.frictionState;
    }

    @Override
    public void paper$setFrictionState(net.kyori.adventure.util.TriState frictionState) {
        this.frictionState = frictionState;
    }

    @Override
    public int paper$getShieldBlockingDelay() {
        return this.shieldBlockingDelay;
    }

    @Override
    public void paper$setShieldBlockingDelay(int shieldBlockingDelay) {
        this.shieldBlockingDelay = shieldBlockingDelay;
    }
}
