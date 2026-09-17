package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.CraftMetaShield;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code BlockStateMeta#clearBlockState()} 的第二个实现类：{@code CraftMetaShield}
 * 直接继承 CraftMetaItem 并实现 BlockStateMeta，拿不到 CraftMetaBlockState 的实现体。
 */
@Mixin(CraftMetaShield.class)
public abstract class CraftMetaShieldApiMixin {

    @Unique
    public void clearBlockState() {
        // CraftMetaShield 的 block-entity 数据就是它自己的 banner 状态，
        // 用公开 API 置空即可（setBlockState(null) 会被 Preconditions 拒绝）。
        BlockStateMeta self = (BlockStateMeta) (Object) this;
        if (self.hasBlockState()) {
            org.bukkit.block.BlockState blank = self.getBlockState();
            blank.setType(org.bukkit.Material.AIR);
            self.setBlockState(blank);
        }
    }
}
