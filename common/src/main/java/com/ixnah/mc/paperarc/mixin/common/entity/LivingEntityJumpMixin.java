package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.LivingEntityBridge;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Port of Paper's EntityJumpEvent (Entity-Jump-API.patch) for LivingEntity.aiStep.
 *
 * Paper rewrites the jump branch:
 *   if (new EntityJumpEvent(getBukkitLivingEntity()).callEvent()) {
 *       this.jumpFromGround(); this.noJumpDelay = 10;
 *   } else { this.setJumping(false); }
 *
 * We wrap the single jumpFromGround() INVOKE in aiStep (bytecode-verified
 * 1.21.1: exactly one call site).
 *
 * <p>B8/Y-4（gaps.md E3）：取消跳跃时**不再**留下 {@code noJumpDelay = 10}。
 * vanilla 在 {@code jumpFromGround()} 之后紧跟着写这个字段（`javap -c`：offset 488 调用、
 * 494 putfield），把那一次写用 {@code @WrapWithCondition} 守住即可；
 * slice 从 {@code jumpFromGround} 起，只挑它后面第一次写，
 * 不会碰到方法开头与 else 分支那两处（offset 14 / 502）。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin implements LivingEntityBridge {

    @Shadow
    protected abstract void setJumping(boolean jumping);

    /** 本次 aiStep 里跳跃是否被插件取消；只在 jumpFromGround 那一处到 noJumpDelay 之间有意义。 */
    @Unique
    private boolean paperarc$jumpCancelled;

    @Override
    public void bridge$setJumping(boolean jumping) {
        this.setJumping(jumping);
    }

    @WrapOperation(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpFromGround()V")
    )
    private void paperarc$entityJump(LivingEntity instance, Operation<Void> original) {
        EntityJumpEvent event = new EntityJumpEvent(
                PaperArcBridge.bukkitEntity(instance));
        this.paperarc$jumpCancelled = !event.callEvent();
        if (this.paperarc$jumpCancelled) {
            this.setJumping(false);
        } else {
            original.call(instance);
        }
    }

    @WrapWithCondition(
            method = "aiStep",
            slice = @Slice(from = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;jumpFromGround()V")),
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/LivingEntity;noJumpDelay:I", ordinal = 0)
    )
    private boolean paperarc$skipJumpDelayWhenCancelled(LivingEntity instance, int delay) {
        return !this.paperarc$jumpCancelled;
    }
}
