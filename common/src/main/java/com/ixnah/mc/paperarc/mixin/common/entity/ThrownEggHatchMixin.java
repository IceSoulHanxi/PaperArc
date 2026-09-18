package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalByteRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEgg;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Paper 的 {@code ThrownEggHatchEvent}（Add-ThrownEggHatchEvent.patch）：与
 * {@code PlayerEggThrowEvent} 同义，但不要求投掷者是玩家（发射器扔的蛋也会孵化）。
 *
 * <p><b>为什么锚 {@code getOwner()}</b>：Arclight 的
 * {@code ThrownEggMixin} 用 {@code @Overwrite} 整体替换了 {@code ThrownEgg#onHit}，
 * 原版体里的 {@code super.onHit}/{@code EntityType.CHICKEN.create} 调用点全部消失
 * （旧实现锚 {@code ThrowableItemProjectile#onHit} 因此 Scanned 1 / 0 succeeded，
 * 见任务书 A7）。覆写后的方法体里有一句原版 {@code onHit} 没有的
 * {@code Entity shooter = this.getOwner();} —— 锚它既能拿到三个局部变量，
 * 又天然自证"我们扫描到的是 Arclight 变换之后的字节码"。
 *
 * <p><b>优先级</b>：Arclight 的 {@code mixins.arclight.core.json} 写的是
 * {@code mixinPriority: 500}，我们的 mixin 是默认 1000，本来就排在它后面；
 * 这里把 {@code priority} 显式写成 2000 只是让这条依赖自文档化。
 *
 * <p><b>与 Paper 的偏差（唯一一条）</b>：Paper 在 {@code PlayerEggThrowEvent}
 * <em>之后</em>发 {@code ThrownEggHatchEvent}，我们在它<em>之前</em> —— 覆写后的
 * 方法体里两者之间没有任何可锚的指令（player 分支里的调用点只在玩家路径上执行）。
 * 由于 Arclight 紧接着用当前局部变量构造 {@code PlayerEggThrowEvent} 再原样读回，
 * 没有插件改 {@code PlayerEggThrowEvent} 时两种顺序等价；两个事件都被监听且都改值时
 * {@code PlayerEggThrowEvent} 说了算。
 */
@Mixin(value = ThrownEgg.class, priority = 2000)
public abstract class ThrownEggHatchMixin {

    @WrapOperation(
            method = "onHit(Lnet/minecraft/world/phys/HitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ThrownEgg;getOwner()Lnet/minecraft/world/entity/Entity;"
            )
    )
    private Entity paperarc$fireThrownEggHatch(ThrownEgg self, Operation<Entity> original,
                                               @Local(index = 2) LocalBooleanRef hatching,
                                               @Local(index = 3) LocalByteRef numHatches,
                                               @Local(index = 4) LocalRef<EntityType> hatchingType) {
        Entity shooter = original.call(self);

        ThrownEggHatchEvent event = new ThrownEggHatchEvent(
                (Egg) PaperArcBridge.bukkitEntity(self),
                hatching.get(), numHatches.get(), hatchingType.get());
        event.callEvent();

        boolean stillHatching = event.isHatching();
        hatching.set(stillHatching);
        // Paper: 取消孵化时子代数必须归零，否则 b0 仍是 1/4
        numHatches.set(stillHatching ? event.getNumHatches() : (byte) 0);
        hatchingType.set(event.getHatchingType());

        return shooter;
    }
}
