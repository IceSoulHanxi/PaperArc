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
 * paper 的 {@code DisplaySlot.SIDEBAR_TEAM_*} 十六个常量。
 *
 * <p><b>这里不是"加常量"，是"加别名"</b>（A8 按 main/B7 的结论改过来）。
 * paper 只是把 Bukkit 原来的 {@code SIDEBAR_<COLOR>} <b>改名</b>成了
 * {@code SIDEBAR_TEAM_<COLOR>}：运行时这十六个槽位一直都在（`javap -p` 核对
 * Arclight 1.20.1 的 {@code org.bukkit.scoreboard.DisplaySlot}，
 * {@code SIDEBAR_BLACK … SIDEBAR_WHITE} 齐全），
 * {@code CraftScoreboardTranslations.SLOTS} 里也早就有
 * {@code SIDEBAR_BLACK ↔ "sidebar.team.black"} 这样的映射。
 *
 * <p>A7 当成新常量用 {@code EnumHelper} 造进 {@code ENUM$VALUES} 是错的：
 * ① {@code values()} 多出十六个记分板认不得的槽位；
 * ② 那张 {@code ImmutableBiMap} 是双向的，同一个 {@code "sidebar.team.black"}
 * 塞不下两个 Bukkit 常量 —— A7 因此只能在 {@code fromBukkitSlot} 里折算，
 * 反向的 {@code getDisplaySlot()} 永远返回旧名，按新名比对的插件拿到 false。
 *
 * <p>改成别名之后：十六个字段指向运行时**已有**的同一个常量，
 * {@code getstatic} 取得到、{@code switch}/{@code EnumSet}/{@code EnumMap}
 * 与记分板转换表全按真常量走，{@code setDisplaySlot} / {@code getDisplaySlot}
 * 往返也一致（探针 P18a）。代价只有一个：{@code Enum.valueOf("SIDEBAR_TEAM_RED")}
 * 仍然不认、{@code name()} 返回旧名 {@code SIDEBAR_RED}（记 docs/gaps.md §3.4）。
 *
 * <p>{@code @Widen} 缺一不可 —— 反向实验（摘掉它）实测
 * {@code IllegalAccessError: tried to access private field
 * org.bukkit.scoreboard.DisplaySlot.SIDEBAR_TEAM_RED}。
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
