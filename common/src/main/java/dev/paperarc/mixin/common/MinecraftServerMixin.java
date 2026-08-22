package dev.paperarc.mixin.common;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import dev.paperarc.event.PaperArcEvents;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/**
 * ServerTickEndEvent 触发点。
 * <p>
 * 对照 Paper：事件在 tickServer 尾部发出，携带 tick 序号、本 tick 耗时（ms）
 * 与距下个 tick 的剩余纳秒。字段名 tickCount / nextTickTimeNanos 已与
 * Paper ver/1.21.x 补丁逐字核对。
 * <p>
 * 冲突评估（docs/conflict-matrix.md #1）：Arclight MinecraftServerMixin 在
 * tickServer 仅占用 HEAD，RETURN 无冲突。
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    private int tickCount;

    @Shadow
    private long nextTickTimeNanos;

    @Unique
    private long paperarc$tickStartNanos;

    @Unique
    private boolean paperarc$tickTiming;

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
        long remaining = this.nextTickTimeNanos - end;
        PaperArcEvents.fire(new ServerTickEndEvent(this.tickCount, durationMs, remaining));
    }
}
