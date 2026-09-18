package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcEnumConstants;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.bukkit.scoreboard.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A7/Y-5 的枚举常量走通实验（移植自 main 的 B6-2）：只补
 * {@code DisplaySlot.SIDEBAR_TEAM_RED} <b>一条</b>，验证
 * "@Unique 静态字段 + @Widen 放宽 + @Inject 到 {@code <clinit>} TAIL 扩 ENUM$VALUES"
 * 这条路在 mixin 0.8.5 + Arclight 1.20.1 上是否成立。
 * <b>通过前不铺开其余 39 个常量</b>（checklist §1.12 bd）。
 *
 * <p>机制与为什么必须落在 {@code <clinit>}：见
 * {@link PaperarcEnumConstants}。</p>
 */
@Mixin(DisplaySlot.class)
public abstract class DisplaySlotEnumMixin {

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_RED")
    private static DisplaySlot SIDEBAR_TEAM_RED;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        SIDEBAR_TEAM_RED = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_RED");
    }
}
