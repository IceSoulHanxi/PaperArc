package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.v.CraftLootTable;
import org.bukkit.craftbukkit.v.entity.CraftMob;
import org.bukkit.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Lootable#setLootTable(LootTable, long)} 的实体侧实现体。
 * Lootable 同时被方块容器（CraftLootable）与生物（CraftMob 一族）实现，两边都要有实现体，
 * 否则对生物调用就是 {@code AbstractMethodError}（PARTIAL_IMPL 门禁抓到）。
 *
 * <p>与 {@link CraftLootableApiMixin} 同样**不写 {@code @Unique}**（目标已有同名 1 参方法），
 * 并且直接写 NMS 字段而不是转调 1 参重载，避免踩上那边的递归坑。</p>
 */
@Mixin(CraftMob.class)
public abstract class CraftMobLootableApiMixin {

    @Shadow
    public abstract Mob getHandle();

    public void setLootTable(LootTable table, long seed) {
        Mob handle = this.getHandle();
        handle.lootTable = java.util.Optional.ofNullable(CraftLootTable.bukkitToMinecraft(table));
        handle.lootTableSeed = seed;
    }
}
