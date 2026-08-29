package com.ixnah.mc.paperarc.mixin.common.server;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.ixnah.mc.paperarc.event.PaperArcEvents;
import java.util.function.BooleanSupplier;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper 1.20.1 ServerTickStartEvent / ServerTickEndEvent。
 * <p>
 * 对照 Paper ver/1.20.1 补丁（Server-Tick-Events.patch）：
 * <ul>
 *   <li>Start：在 tickChildren 调用点发出，参数 tickCount+1（tickCount++ 之前）。</li>
 *   <li>End：tickServer 尾部；durationMs = (now - tickStartNanos)/1e6，
 *       remaining = TICK_TIME - (now - tickStartNanos)（本 tick 耗时对 50ms 的差额）。</li>
 * </ul>
 * 字段说明：1.20.1 mojmap 只有 nextTickTime（毫秒语义），不存在 1.21 的
 * nextTickTimeNanos；Paper 1.20.1 的 remaining 是 tickServer 方法体内局部变量
 * （lastTick/catchupTime）计算，mixin 无法在 RETURN 处取局部变量，故用本 tick
 * 耗时近似（语义一致：正值=提前，负值=落后）。
 * <p>
 * tickCount 为 @Shadow mojmap 名，loom remapJar 会映射到 srg f_129766_。
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    private static final long TICK_TIME_NANOS = 50_000_000L; // 20 TPS

    @Accessor("tickCount")
    abstract int paperarc$getTickCount();

    @Unique
    private long paperarc$tickStartNanos;

    @Unique
    private boolean paperarc$tickTiming;

    /**
     * ServerTickStartEvent 触发点。
     * <p>
     * 对照 Paper：事件在 tickCount++ 前发出（参数为 tickCount+1），
     * 位于 pause-while-empty 早退分支之后——只有真正执行的 tick 才发。
     * 用 tickChildren 调用点定位可精确落在同一位置。
     */
    @Inject(
        method = "tickServer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickChildren(Ljava/util/function/BooleanSupplier;)V")
    )
    private void paperarc$onTickBegin(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        PaperArcEvents.fire(new ServerTickStartEvent(this.paperarc$getTickCount() + 1));
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void paperarc$onTickStart(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.paperarc$tickStartNanos = System.nanoTime();
        this.paperarc$tickTiming = true;
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    private void paperarc$onTickEnd(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (!this.paperarc$tickTiming) {
            return;
        }
        this.paperarc$tickTiming = false;
        long end = System.nanoTime();
        double durationMs = (end - this.paperarc$tickStartNanos) / 1_000_000.0D;
        long remaining = TICK_TIME_NANOS - (end - this.paperarc$tickStartNanos);
        PaperArcEvents.fire(new ServerTickEndEvent(this.paperarc$getTickCount(), durationMs, remaining));
    }
}