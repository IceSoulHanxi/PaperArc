package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcEnumConstants;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Y-5：给 {@code org.bukkit.event.entity.EntityTargetEvent.TargetReason} 补 paper 新增的枚举常量。
 *
 * <p>属"无消费方"那一类 —— 这些常量在 Arclight 运行时只会被插件读取/比较，
 * 不经过任何 CraftBukkit 转换表（要么由触发点产生、而 Arclight 的触发点本来就不产生它，
 * 要么纯粹是声明性的）。配方与 {@link PaperarcEnumConstants} 一致：
 * {@code @Unique} 静态字段 + {@code @Widen} 放宽 + {@code <clinit>} TAIL 扩 {@code $VALUES}。
 */
@Mixin(org.bukkit.event.entity.EntityTargetEvent.TargetReason.class)
public abstract class TargetReasonEnumMixin {

    @Unique
    @Widen(because = "paper-api: public static final TargetReason TARGET_INVALID")
    private static org.bukkit.event.entity.EntityTargetEvent.TargetReason TARGET_INVALID;

    @Unique
    @Widen(because = "paper-api: public static final TargetReason TARGET_OTHER_LEVEL")
    private static org.bukkit.event.entity.EntityTargetEvent.TargetReason TARGET_OTHER_LEVEL;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        TARGET_INVALID = PaperarcEnumConstants.add(org.bukkit.event.entity.EntityTargetEvent.TargetReason.class, "TARGET_INVALID");
        TARGET_OTHER_LEVEL = PaperarcEnumConstants.add(org.bukkit.event.entity.EntityTargetEvent.TargetReason.class, "TARGET_OTHER_LEVEL");
    }
}
