package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.data.BlockData} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.data.BlockData", remap = false)
public interface BlockDataIfaceMixin {

    @Unique
    public abstract org.bukkit.util.VoxelShape getCollisionShape(org.bukkit.Location p0);

    @Unique
    public abstract float getDestroySpeed(org.bukkit.inventory.ItemStack p0, boolean p1);

    @Unique
    public abstract boolean isRandomlyTicked();
}
