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
 * paper 把 {@code DisplaySlot.SIDEBAR_<色>} <b>改名</b>成了 {@code SIDEBAR_TEAM_<色>}
 * （paper-api 里旧名已经不存在），运行时还是旧名 —— 插件引用新名即 {@code NoSuchFieldError}。
 * 这里按 A7/Y-5 走通的配方把 16 个新名补成真正的枚举常量。
 *
 * <p>机制与"为什么必须落在 {@code <clinit>} 的 TAIL"：见 {@link PaperarcEnumConstants}。
 * {@code @Widen} 缺一不可 —— 反向实验（摘掉它）实测
 * {@code IllegalAccessError: tried to access private field
 * org.bukkit.scoreboard.DisplaySlot.SIDEBAR_TEAM_RED}。
 *
 * <p><b>消费方</b>：{@code CraftScoreboardTranslations.SLOTS} 是一张
 * {@code ImmutableBiMap<DisplaySlot, String>}，新常量不在里面，
 * {@code fromBukkitSlot} 会拿 null 去查 vanilla 槽位名。{@code CraftScoreboardSlotAliasMixin}
 * 把 {@code SIDEBAR_TEAM_X} 折算成运行时的 {@code SIDEBAR_X}，所以
 * {@code objective.setDisplaySlot(SIDEBAR_TEAM_RED)} 真的生效。
 *
 * <p><b>已知语义差异</b>：反向的 {@code toBukkitSlot}（{@code Objective#getDisplaySlot()}）
 * 仍返回运行时的旧名 {@code SIDEBAR_RED} —— BiMap 不允许两个键映射到同一个值，
 * 而改成返回新名会让按 Spigot-API 编译的插件比对失败。见 docs/gaps.md。
 */
@Mixin(DisplaySlot.class)
public abstract class DisplaySlotEnumMixin {

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_BLACK")
    private static DisplaySlot SIDEBAR_TEAM_BLACK;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_DARK_BLUE")
    private static DisplaySlot SIDEBAR_TEAM_DARK_BLUE;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_DARK_GREEN")
    private static DisplaySlot SIDEBAR_TEAM_DARK_GREEN;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_DARK_AQUA")
    private static DisplaySlot SIDEBAR_TEAM_DARK_AQUA;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_DARK_RED")
    private static DisplaySlot SIDEBAR_TEAM_DARK_RED;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_DARK_PURPLE")
    private static DisplaySlot SIDEBAR_TEAM_DARK_PURPLE;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_GOLD")
    private static DisplaySlot SIDEBAR_TEAM_GOLD;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_GRAY")
    private static DisplaySlot SIDEBAR_TEAM_GRAY;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_DARK_GRAY")
    private static DisplaySlot SIDEBAR_TEAM_DARK_GRAY;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_BLUE")
    private static DisplaySlot SIDEBAR_TEAM_BLUE;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_GREEN")
    private static DisplaySlot SIDEBAR_TEAM_GREEN;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_AQUA")
    private static DisplaySlot SIDEBAR_TEAM_AQUA;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_RED")
    private static DisplaySlot SIDEBAR_TEAM_RED;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_LIGHT_PURPLE")
    private static DisplaySlot SIDEBAR_TEAM_LIGHT_PURPLE;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_YELLOW")
    private static DisplaySlot SIDEBAR_TEAM_YELLOW;

    @Unique
    @Widen(because = "paper-api: public static final DisplaySlot SIDEBAR_TEAM_WHITE")
    private static DisplaySlot SIDEBAR_TEAM_WHITE;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void paperarc$addPaperConstants(CallbackInfo ci) {
        SIDEBAR_TEAM_BLACK = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_BLACK");
        SIDEBAR_TEAM_DARK_BLUE = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_DARK_BLUE");
        SIDEBAR_TEAM_DARK_GREEN = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_DARK_GREEN");
        SIDEBAR_TEAM_DARK_AQUA = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_DARK_AQUA");
        SIDEBAR_TEAM_DARK_RED = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_DARK_RED");
        SIDEBAR_TEAM_DARK_PURPLE = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_DARK_PURPLE");
        SIDEBAR_TEAM_GOLD = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_GOLD");
        SIDEBAR_TEAM_GRAY = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_GRAY");
        SIDEBAR_TEAM_DARK_GRAY = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_DARK_GRAY");
        SIDEBAR_TEAM_BLUE = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_BLUE");
        SIDEBAR_TEAM_GREEN = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_GREEN");
        SIDEBAR_TEAM_AQUA = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_AQUA");
        SIDEBAR_TEAM_RED = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_RED");
        SIDEBAR_TEAM_LIGHT_PURPLE = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_LIGHT_PURPLE");
        SIDEBAR_TEAM_YELLOW = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_YELLOW");
        SIDEBAR_TEAM_WHITE = PaperarcEnumConstants.add(DisplaySlot.class, "SIDEBAR_TEAM_WHITE");
    }
}
