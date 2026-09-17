package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.block.BlockState;
import java.util.Collection;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.Metadatable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.BlockState} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.BlockState", remap = false)
public interface BlockStateIfaceMixin {

    @Unique
    public abstract boolean isCollidable();

    @Unique
    public abstract java.util.Collection getDrops(org.bukkit.inventory.ItemStack p0, org.bukkit.entity.Entity p1);

    @Unique
    public default Collection<ItemStack> getDrops() {
        BlockState self = (BlockState) this;
        return self.getDrops((ItemStack) null);
    }

    @Unique
    public default Collection<ItemStack> getDrops(ItemStack tool) {
        BlockState self = (BlockState) this;
        return self.getDrops(tool, (Entity) null);
    }
}
