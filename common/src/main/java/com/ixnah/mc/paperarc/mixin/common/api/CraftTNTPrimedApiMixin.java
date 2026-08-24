package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.item.PrimedTnt;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.entity.CraftTNTPrimed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's TNTPrimed visual-block API.
 *
 * Paper implements these on CraftTNTPrimed by delegating to the vanilla
 * PrimedTnt block-state override (mojmap: getBlockState/setBlockState).
 */
@Mixin(CraftTNTPrimed.class)
public abstract class CraftTNTPrimedApiMixin {

    @Shadow
    public abstract PrimedTnt getHandle();

    @Unique
    public BlockData getBlockData() {
        return CraftBlockData.fromData(getHandle().getBlockState());
    }

    @Unique
    public void setBlockData(BlockData data) {
        com.google.common.base.Preconditions.checkArgument(data != null,
            "The visual block data of this tnt cannot be null. To reset it just set to the TNT default block data");
        getHandle().setBlockState(((CraftBlockData) data).getState());
    }
}
