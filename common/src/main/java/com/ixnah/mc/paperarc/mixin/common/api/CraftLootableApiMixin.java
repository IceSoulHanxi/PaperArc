package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.bukkit.craftbukkit.v.CraftLootTable;
import org.bukkit.craftbukkit.v.block.CraftLootable;
import org.bukkit.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code Lootable#setLootTable(LootTable, long)}（方块容器侧）。
 *
 * <p>两个坑（都是真机踩出来的）：</p>
 * <ol>
 *   <li>**不能写 {@code @Unique}**：目标类已经有同名的 1 参 {@code setLootTable}，
 *       Mixin 会把 {@code @Unique} 方法整个丢掉（日志 "Discarding @Unique public method …
 *       because it already exists"），运行时等于没加。</li>
 *   <li>**方法体里不能调 1 参重载**：CraftLootable 自己就有一个 {@code private
 *       setLootTable(LootTable, long)}，1 参版本就是转调它。我们这个 public 方法签名与那个
 *       私有方法一致，合并后是**替换**了它 —— 再去调 1 参就无限递归（实测 StackOverflowError）。
 *       所以这里直接复刻私有方法原本的实现。</li>
 * </ol>
 */
@Mixin(CraftLootable.class)
public abstract class CraftLootableApiMixin {

    public void setLootTable(LootTable table, long seed) {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        if (snapshot instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(CraftLootTable.bukkitToMinecraft(table), seed);
        }
    }
}
