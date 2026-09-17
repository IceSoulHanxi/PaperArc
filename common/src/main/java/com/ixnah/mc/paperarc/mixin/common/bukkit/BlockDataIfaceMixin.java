package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.block.data.BlockData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.data.BlockData} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.data.BlockData", remap = false)
public interface BlockDataIfaceMixin {

    @Unique
    public abstract org.bukkit.util.VoxelShape getCollisionShape(org.bukkit.Location p0);

    @Unique
    public abstract float getDestroySpeed(org.bukkit.inventory.ItemStack p0, boolean p1);

    @Unique
    public abstract boolean isRandomlyTicked();

    @Unique
    public default float getDestroySpeed(ItemStack itemStack) {
        BlockData self = (BlockData) this;
        return self.getDestroySpeed(itemStack, false);
    }
}
