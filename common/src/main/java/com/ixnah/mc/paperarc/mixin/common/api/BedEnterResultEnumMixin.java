package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcEnumConstants;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 给 {@code PlayerBedEnterEvent.BedEnterResult} 加的 {@code OBSTRUCTED}。
 * 运行时的触发点不会产出它，插件只是读取/比较，无需同步转换表。
 *
 * <p>配方与 {@link DisplaySlotEnumMixin} 相同，见 {@link PaperarcEnumConstants}。
 */
@Mixin(org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.class)
public abstract class BedEnterResultEnumMixin {

    @Unique
    @Widen(because = "paper-api: public static final PlayerBedEnterEvent.BedEnterResult OBSTRUCTED")
    private static org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult OBSTRUCTED;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        OBSTRUCTED = PaperarcEnumConstants.add(org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.class, "OBSTRUCTED");
    }
}
