package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A4-4：给 {@code org.bukkit.loot.Lootable} 补上 paper-api 的 default 方法
 * （运行时接口里一个都没有，插件调用即 NoSuchMethodError）。
 */
@Mixin(targets = "org.bukkit.loot.Lootable", remap = false)
public interface LootableIfaceMixin {

    @Unique
    public default boolean hasLootTable() {
        return ((org.bukkit.loot.Lootable) this).getLootTable() != null;
    }

    @Unique
    public default void clearLootTable() {
        ((org.bukkit.loot.Lootable) this).setLootTable(null);
    }
    // 注意：paper 还有一个 default setLootTable(LootTable, long)，这里**故意不补** ——
    // 运行时的 CraftLootable / CraftMinecartContainer 上都有同签名的 **private** 方法，
    // 加上接口默认实现后 invokeinterface 会解析到那个 private 方法并抛 IllegalAccessError。
}
