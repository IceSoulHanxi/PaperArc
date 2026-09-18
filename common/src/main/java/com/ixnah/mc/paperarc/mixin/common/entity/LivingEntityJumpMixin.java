package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.LivingEntityBridge;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
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
 * We wrap the single jumpFromGround() INVOKE in aiStep (bytecode-verified:
 * exactly one call site). On cancellation Paper skips jumpFromGround and sets
 * setJumping(false).
 *
 * <p>A8/Y-4（gaps.md E3）：取消时 vanilla 后面那句 {@code this.noJumpDelay = 10}
 * 原先照样执行（等于"取消了还是要等 10 tick 才能再跳"）。现在用
 * {@code @WrapWithCondition} 把它也守住 —— 用 {@code @Slice} 从
 * {@code jumpFromGround()} 起算，只命中跳跃分支里那一次赋值，
 * 不会碰到 {@code aiStep} 开头 {@code --this.noJumpDelay} 那次。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin implements LivingEntityBridge {

    @Unique
    private boolean paperarc$jumpCancelled;

    @Invoker("setJumping")
    abstract void paperarc$setJumping(boolean jumping);

    @Override
    public void bridge$setJumping(boolean jumping) {
        this.paperarc$setJumping(jumping);
    }

    @WrapOperation(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpFromGround()V")
    )
    private void paperarc$entityJump(LivingEntity instance, Operation<Void> original) {
        EntityJumpEvent event = new EntityJumpEvent(
                PaperArcBridge.bukkitEntity(instance));
        if (event.callEvent()) {
            this.paperarc$jumpCancelled = false;
            original.call(instance);
        } else {
            this.paperarc$jumpCancelled = true;
            this.paperarc$setJumping(false);
        }
    }

    @WrapWithCondition(
            method = "aiStep",
            slice = @Slice(from = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;jumpFromGround()V")),
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/LivingEntity;noJumpDelay:I")
    )
    private boolean paperarc$skipJumpDelayWhenCancelled(LivingEntity instance, int value) {
        if (this.paperarc$jumpCancelled) {
            this.paperarc$jumpCancelled = false;
            return false;
        }
        return true;
    }
}
