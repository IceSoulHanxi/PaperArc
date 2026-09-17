package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import org.bukkit.craftbukkit.v.CraftLootTable;
import org.bukkit.craftbukkit.v.block.CraftDecoratedPot;
import org.bukkit.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 给 {@code org.bukkit.block.DecoratedPot} 加了父接口 {@code org.bukkit.loot.Lootable}
 * （B3-2）。{@code CraftDecoratedPot} 直接继承 {@code CraftBlockEntityState}，不在
 * {@code CraftLootable} 那条链上，所以这五个方法要单独实现；NMS 侧
 * {@code DecoratedPotBlockEntity} 本身实现 {@code RandomizableContainer}，直接转调。
 */
@Mixin(CraftDecoratedPot.class)
public abstract class CraftDecoratedPotLootableApiMixin {

    @Unique
    private DecoratedPotBlockEntity paperarc$pot() {
        return (DecoratedPotBlockEntity) ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

    @Unique
    public LootTable getLootTable() {
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key =
                paperarc$pot().getLootTable();
        return key == null ? null : CraftLootTable.minecraftToBukkit(key);
    }

    @Unique
    public void setLootTable(LootTable table) {
        paperarc$pot().setLootTable(table == null ? null : CraftLootTable.bukkitToMinecraft(table));
    }

    @Unique
    public void setLootTable(LootTable table, long seed) {
        DecoratedPotBlockEntity pot = paperarc$pot();
        pot.setLootTable(table == null ? null : CraftLootTable.bukkitToMinecraft(table));
        pot.setLootTableSeed(seed);
    }

    @Unique
    public long getSeed() {
        return paperarc$pot().getLootTableSeed();
    }

    @Unique
    public void setSeed(long seed) {
        paperarc$pot().setLootTableSeed(seed);
    }
}
