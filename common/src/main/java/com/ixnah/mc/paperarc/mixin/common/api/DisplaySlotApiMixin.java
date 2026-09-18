package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.scoreboard.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 的 {@code DisplaySlot#getId()} 与 {@code toString()}。
 *
 * <p>取值与 paper 一致：BELOW_NAME=belowName、PLAYER_LIST=list、SIDEBAR=sidebar，
 * 队伍侧边栏 = {@code "sidebar.team." + 小写颜色名}。
 * <b>注意运行时这十六个常量的 {@code name()} 是旧名 {@code SIDEBAR_<COLOR>}</b>
 * （paper 改名成了 {@code SIDEBAR_TEAM_<COLOR>}，我们用别名指过去，
 * 见 {@code DisplaySlotEnumMixin}），所以两种前缀都要认。
 *
 * <p>{@code toString()} 写成**不带 {@code @Unique} 的普通方法**：运行时的
 * {@code DisplaySlot} 自己没有声明 {@code toString()}（`javap -p` 核对，只继承
 * {@code Enum} 的），{@code @Overwrite} 会因"目标里找不到该方法"失败，
 * 而 {@code @Unique} 会被当成与继承来的方法冲突而整条丢弃（checklist bo）。
 */
@Mixin(DisplaySlot.class)
public abstract class DisplaySlotApiMixin {

    @Unique
    public String getId() {
        String name = ((DisplaySlot) (Object) this).name();
        if ("BELOW_NAME".equals(name)) {
            return "belowName";
        }
        if ("PLAYER_LIST".equals(name)) {
            return "list";
        }
        if ("SIDEBAR".equals(name)) {
            return "sidebar";
        }
        if (name.startsWith("SIDEBAR_TEAM_")) {
            return "sidebar.team."
                    + name.substring("SIDEBAR_TEAM_".length()).toLowerCase(Locale.ENGLISH);
        }
        if (name.startsWith("SIDEBAR_")) {
            // 运行时的旧名（paper 的 SIDEBAR_TEAM_<COLOR> 就是它改名来的）
            return "sidebar.team." + name.substring("SIDEBAR_".length()).toLowerCase(Locale.ENGLISH);
        }
        return name.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
