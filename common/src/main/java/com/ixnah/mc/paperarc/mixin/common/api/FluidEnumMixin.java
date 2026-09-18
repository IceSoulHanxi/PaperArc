package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcEnumConstants;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 给 {@code org.bukkit.Fluid} 加的 {@code EMPTY}（vanilla 的 {@code minecraft:empty} 流体）。
 * 只被插件读取/比较，CraftBukkit 侧没有需要同步的转换表。
 *
 * <p>配方与 {@link DisplaySlotEnumMixin} 相同，见 {@link PaperarcEnumConstants}。
 */
@Mixin(org.bukkit.Fluid.class)
public abstract class FluidEnumMixin {

    @Unique
    @Widen(because = "paper-api: public static final Fluid EMPTY")
    private static org.bukkit.Fluid EMPTY;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        EMPTY = PaperarcEnumConstants.add(org.bukkit.Fluid.class, "EMPTY");
    }
}
