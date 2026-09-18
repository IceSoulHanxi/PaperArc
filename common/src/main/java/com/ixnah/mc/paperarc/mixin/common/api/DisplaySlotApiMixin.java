package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.scoreboard.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 的 {@code DisplaySlot#getId()} 与 {@code toString()}。
 *
 * <p>paper 给这个枚举加了一个 {@code id} 字段（vanilla 的记分板槽位名），两个方法都返回它。
 * `javap -c` 核对 paper-api 的两个私有构造器：三个特殊常量是字面量
 * （{@code below_name}/{@code list}/{@code sidebar}），十六个队伍色是
 * {@code "sidebar.team." + color}。运行时的常量名是旧名（{@code SIDEBAR_RED}，
 * {@code SIDEBAR_TEAM_RED} 只是我们补的别名，见 Y-5），所以按 {@code name()} 现算，
 * 取值与 paper 逐字相同、也与 {@code CraftScoreboardTranslations.SLOTS} 的值一致。
 *
 * <p><b>{@code toString()} 不能写 {@code @Unique}</b>：{@code @Unique} 与"从
 * {@code Enum}/{@code Object} 继承来的同签名方法"也算撞名，Mixin 会整个丢弃
 * （日志 {@code Discarding @Unique}，运行时等于没加）。不带 {@code @Unique} 的普通方法
 * 会**替换**目标里的同签名方法 —— 这里正是想要的效果。{@code getId()} 是新方法，
 * 必须带 {@code @Unique}。
 */
@Mixin(DisplaySlot.class)
public abstract class DisplaySlotApiMixin {

    @Unique
    public String getId() {
        String name = ((DisplaySlot) (Object) this).name();
        return switch (name) {
            case "BELOW_NAME" -> "below_name";
            case "PLAYER_LIST" -> "list";
            case "SIDEBAR" -> "sidebar";
            default -> name.startsWith("SIDEBAR_")
                    ? "sidebar.team." + name.substring("SIDEBAR_".length()).toLowerCase(Locale.ROOT)
                    : name.toLowerCase(Locale.ROOT);
        };
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
