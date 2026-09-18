package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcEnumConstants;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 给 {@code EntityTargetEvent.TargetReason} 加的两个常量。
 * 运行时的触发点不会产出它们，插件只是读取/比较，无需同步转换表。
 *
 * <p>配方与 {@link DisplaySlotEnumMixin} 相同，见 {@link PaperarcEnumConstants}。
 */
@Mixin(org.bukkit.event.entity.EntityTargetEvent.TargetReason.class)
public abstract class TargetReasonEnumMixin {

    @Unique
    @Widen(because = "paper-api: public static final EntityTargetEvent.TargetReason TARGET_OTHER_LEVEL")
    private static org.bukkit.event.entity.EntityTargetEvent.TargetReason TARGET_OTHER_LEVEL;

    @Unique
    @Widen(because = "paper-api: public static final EntityTargetEvent.TargetReason TARGET_INVALID")
    private static org.bukkit.event.entity.EntityTargetEvent.TargetReason TARGET_INVALID;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        TARGET_OTHER_LEVEL = PaperarcEnumConstants.add(org.bukkit.event.entity.EntityTargetEvent.TargetReason.class, "TARGET_OTHER_LEVEL");
        TARGET_INVALID = PaperarcEnumConstants.add(org.bukkit.event.entity.EntityTargetEvent.TargetReason.class, "TARGET_INVALID");
    }
}
