package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.block.impl.CraftCherryLeaves;
import org.bukkit.craftbukkit.v.block.impl.CraftLeaves;
import org.bukkit.craftbukkit.v.block.impl.CraftMangroveLeaves;
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
// CB 的 Leaves 有三个互不继承的实现类（普通/樱花/红树），只挂一个，走到另两个就是
// AbstractMethodError（A4-1 r）。三者都直接继承 CraftBlockData，可以放同一个 mixin。
@Mixin({CraftLeaves.class, CraftCherryLeaves.class, CraftMangroveLeaves.class})
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
