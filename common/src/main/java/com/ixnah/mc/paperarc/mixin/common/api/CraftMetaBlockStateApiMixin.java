package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v.inventory.CraftMetaBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Paper 的 {@code BlockStateMeta#clearBlockState()}：清掉 block-entity 数据。 */
@Mixin(CraftMetaBlockState.class)
public abstract class CraftMetaBlockStateApiMixin {

    @Shadow
    private CraftBlockEntityState<?> blockEntityTag;

    @Unique
    public void clearBlockState() {
        this.blockEntityTag = null;
    }
}
