package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcEnumConstants;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 给 {@code Boat.Status} 加的 {@code NOT_IN_WORLD}。
 * 运行时的 {@code CraftBoat} 永远不会产出它，插件只是读取/比较，无需同步转换表。
 *
 * <p>配方与 {@link DisplaySlotEnumMixin} 相同，见 {@link PaperarcEnumConstants}。
 */
@Mixin(org.bukkit.entity.Boat.Status.class)
public abstract class BoatStatusEnumMixin {

    @Unique
    @Widen(because = "paper-api: public static final Boat.Status NOT_IN_WORLD")
    private static org.bukkit.entity.Boat.Status NOT_IN_WORLD;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        NOT_IN_WORLD = PaperarcEnumConstants.add(org.bukkit.entity.Boat.Status.class, "NOT_IN_WORLD");
    }
}
