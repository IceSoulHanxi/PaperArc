package com.ixnah.mc.paperarc.bridge.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * {@code org.bukkit.inventory.ItemStack#isEmpty()} 的替身。
 *
 * <p>那是 paper 加在 Bukkit 的 ItemStack 上的便利方法，Arclight 运行时没有，调了就是
 * {@code NoSuchMethodError}（checklist §1.6 n；{@code checkRuntimeApiCalls} 门禁抓出来的）。
 * 语义照抄 paper：类型是空气或数量 &lt;= 0 即为空。
 */
public final class PaperarcItemStacks {

    private PaperarcItemStacks() {
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR || stack.getAmount() <= 0;
    }
}
