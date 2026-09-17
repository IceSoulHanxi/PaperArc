package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.block.impl.CraftDecoratedPot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code org.bukkit.block.data.type.DecoratedPot#isCracked()/setCracked(boolean)}
 * 在 Arclight 1.21.1 运行时缺失（声明由 {@code DecoratedPotIfaceMixin} 补上），
 * 这里在**块数据**实现类上给出实现体，走 vanilla 的 {@code DecoratedPotBlock.CRACKED} 属性。
 *
 * <p>注意宿主必须是块数据的 {@code block.impl.CraftDecoratedPot}，不是方块状态的
 * {@code block.CraftDecoratedPot} —— 后者对应的接口 {@code org.bukkit.block.DecoratedPot}
 * 根本没有这两个方法，实现在那边是死代码（B2-1c 的 check-iface-impl-pairing.py 实测）。</p>
 */
@Mixin(CraftDecoratedPot.class)
public abstract class CraftDecoratedPotBlockDataApiMixin {

    @Unique
    private static final BooleanProperty PAPERARC$CRACKED = DecoratedPotBlock.CRACKED;

    @Unique
    public boolean isCracked() {
        return ((CraftBlockData) (Object) this).getState().getValue(PAPERARC$CRACKED);
    }

    @Unique
    public void setCracked(boolean cracked) {
        ((CraftBlockData) (Object) this).set(PAPERARC$CRACKED, cracked);
    }
}
