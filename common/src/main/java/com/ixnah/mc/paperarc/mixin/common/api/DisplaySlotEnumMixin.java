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
 * paper 的 {@code DisplaySlot.SIDEBAR_TEAM_*} 十六个常量（gaps.md 枚举常量那一维）。
 *
 * <p><b>这里不是"加常量"，是"加别名"。</b> B6-2 的走通实验把 {@code SIDEBAR_TEAM_RED}
 * 当成新常量用 {@code EnumHelper} 造了一个进 {@code $VALUES}，B7/Y-5 铺开时才发现那是错的：
 * paper 只是把 Bukkit 原来的 {@code SIDEBAR_<COLOR>} <b>改名</b>成了
 * {@code SIDEBAR_TEAM_<COLOR>} —— 运行时这十六个槽位一直都在（名字是旧的），
 * {@code CraftScoreboardTranslations.SLOTS} 里也早就有
 * {@code SIDEBAR_BLACK ↔ "sidebar.team.black"} 这样的映射。
 *
 * <p>造新常量的后果是实打实的：① {@code values()} 多出十六个记分板用不了的槽位；
 * ② 那张 {@code ImmutableBiMap} 是双向的，同一个 {@code "sidebar.team.black"}
 * 不可能同时映射两个 Bukkit 常量，硬塞就是
 * {@code IllegalArgumentException: Multiple entries with same value}，
 * {@code CraftScoreboardTranslations} 整个类初始化失败（真机实测，连
 * {@code registerNewObjective} 一起挂掉）。
 *
 * <p>所以这里只加十六个 {@code @Unique @Widen private static} 字段，在
 * {@code <clinit>} 的 TAIL 指向运行时**已有**的同一个常量：{@code getstatic} 取得到、
 * {@code switch}/{@code EnumSet}/{@code EnumMap} 全都按真常量走、记分板往返完全正常。
 * 代价只有一个：{@code Enum.valueOf("SIDEBAR_TEAM_RED")} 仍然不认、{@code name()}
 * 返回的是旧名 {@code SIDEBAR_RED}（记 docs/gaps.md）。
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
    private static void paperarc$aliasPaperNames(CallbackInfo ci) {
        SIDEBAR_TEAM_BLACK = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_BLACK");
        SIDEBAR_TEAM_DARK_BLUE = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_DARK_BLUE");
        SIDEBAR_TEAM_DARK_GREEN = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_DARK_GREEN");
        SIDEBAR_TEAM_DARK_AQUA = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_DARK_AQUA");
        SIDEBAR_TEAM_DARK_RED = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_DARK_RED");
        SIDEBAR_TEAM_DARK_PURPLE = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_DARK_PURPLE");
        SIDEBAR_TEAM_GOLD = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_GOLD");
        SIDEBAR_TEAM_GRAY = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_GRAY");
        SIDEBAR_TEAM_DARK_GRAY = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_DARK_GRAY");
        SIDEBAR_TEAM_BLUE = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_BLUE");
        SIDEBAR_TEAM_GREEN = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_GREEN");
        SIDEBAR_TEAM_AQUA = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_AQUA");
        SIDEBAR_TEAM_RED = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_RED");
        SIDEBAR_TEAM_LIGHT_PURPLE = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_LIGHT_PURPLE");
        SIDEBAR_TEAM_YELLOW = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_YELLOW");
        SIDEBAR_TEAM_WHITE = PaperarcEnumConstants.alias(DisplaySlot.class, "SIDEBAR_WHITE");
    }
}
