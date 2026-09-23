package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.block.impl.CraftComposter;
import org.bukkit.craftbukkit.v.block.impl.CraftLiquid;
import org.bukkit.craftbukkit.v.block.impl.CraftLayeredCauldron;
import org.bukkit.craftbukkit.v.block.impl.CraftLight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code Levelled#getMinimumLevel()}（运行时只有 getMaximumLevel）。
 *
 * <p>注意宿主不能写 {@code CraftLevelled}：生成出来的 BlockData 实现类
 * （CraftComposter/CraftFluids/CraftLayeredCauldron/CraftLight）**都直接继承
 * CraftBlockData 并自己实现 Levelled**，根本不走 CraftLevelled，挂上去等于没挂
 * （PARTIAL_IMPL 门禁实测）。和 {@code CraftLeavesApiMixin} 一样 {@code extends
 * CraftBlockData} 才能调到 protected 的 {@code getMin}。</p>
 */
@Mixin({CraftComposter.class, CraftLiquid.class, CraftLayeredCauldron.class, CraftLight.class})
public abstract class CraftLevelledApiMixin extends CraftBlockData {

    @Unique
    public int getMinimumLevel() {
        return getMin(BlockStateProperties.LEVEL);
    }
}
