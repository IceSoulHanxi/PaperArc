package com.ixnah.mc.paperarc.mixin.common.player;

import io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Paper "Add worldborder events"：WorldBorderBoundsChangeFinishEvent。
 * <p>
 * 对照 Paper 补丁：MovingBorderExtent#update 在 world != null 且插值走完时发
 *（不可取消的）FinishEvent，然后按原逻辑转为 StaticBorderExtent。1.21.11 起
 * update 先自减 lerpProgress 再判 {@code <= 0}，剩余时间的 getter 也没了，
 * 所以锚在"new StaticBorderExtent"——那一刻就是走完；时长 lerpDuration 已是 tick。
 * <p>
 * 实现差异：vanilla 内部类无 Paper 的 world 字段，宿主世界经外部类实例
 * （合成字段 this$0）用 WorldBorderMixin#paperarc$findWorld 反查；找不到时
 * 等价 world == null，不发事件。MovingBorderExtent 是 private 内部类，
 * 无法用类字面量 @Mixin，改用 targets 字符串；update 返回类型 BorderExtent
 * 亦 private，故 CallbackInfoReturnable 用 Object 泛型（擦除后等价）。
 * 外部类实例不能 @Shadow 合成字段 this$0（Mixin 不支持合成字段），
 * 改为注入构造器（WorldBorder, double, double, long duration, long gameTime）捕获。
 */
@Mixin(targets = "net.minecraft.world.level.border.WorldBorder$MovingBorderExtent")
public abstract class WorldBorderMovingBorderExtentMixin {

    /** 经构造器捕获的外部 WorldBorder 实例。 */
    @Unique
    private WorldBorder paperarc$outer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void paperarc$captureOuter(WorldBorder outer, double fromSize, double toSize, long duration, long gameTime,
            CallbackInfo ci) {
        this.paperarc$outer = outer;
    }

    @Shadow @Final
    private double from;

    @Shadow @Final
    private double to;

    @Shadow @Final
    private double lerpDuration;

    @Inject(method = "update", at = @At(value = "NEW",
            target = "net/minecraft/world/level/border/WorldBorder$StaticBorderExtent"))
    private void paperarc$onBoundsChangeFinish(CallbackInfoReturnable<Object> cir) {
        World bukkitWorld = com.ixnah.mc.paperarc.bridge.WorldBorderSupport.findWorld(this.paperarc$outer);
        if (bukkitWorld == null) {
            return;
        }
        new WorldBorderBoundsChangeFinishEvent(
                bukkitWorld,
                bukkitWorld.getWorldBorder(),
                this.from,
                this.to,
                this.lerpDuration
        ).callEvent();
    }
}
