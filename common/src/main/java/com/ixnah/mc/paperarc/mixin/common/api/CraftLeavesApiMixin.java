package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.block.impl.CraftLeaves;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-missing-block-data-API.patch additions on
 * {@link CraftLeaves}: {@code Leaves#getMaximumDistance()} and
 * {@code Leaves#getMinimumDistance()}.
 *
 * <p>Extends {@link CraftBlockData} (the target's superclass) so the
 * protected static {@code getMin}/{@code getMax} helpers used by Paper's
 * implementation resolve after the mixin is merged into the target.
 */
// CraftCherryLeaves / CraftMangroveLeaves 是独立生成的 BlockData 实现类，
// 不继承 CraftLeaves，必须一起挂（PARTIAL_IMPL 门禁）。
@Mixin({CraftLeaves.class,
        org.bukkit.craftbukkit.v.block.impl.CraftCherryLeaves.class,
        org.bukkit.craftbukkit.v.block.impl.CraftMangroveLeaves.class})
public abstract class CraftLeavesApiMixin extends CraftBlockData {

    @Unique
    private static final IntegerProperty PAPERARC$DISTANCE = BlockStateProperties.DISTANCE;

    @Unique
    public int getMaximumDistance() {
        return getMax(PAPERARC$DISTANCE);
    }

    @Unique
    public int getMinimumDistance() {
        return getMin(PAPERARC$DISTANCE);
    }
}
