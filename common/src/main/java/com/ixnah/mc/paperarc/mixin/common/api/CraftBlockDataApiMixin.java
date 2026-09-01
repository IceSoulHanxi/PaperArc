package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-API-to-get-the-collision-shape-of-a-block-before,
 * Add-Destroy-Speed-API and Block-Ticking-API additions on
 * {@link CraftBlockData}: {@code getCollisionShape(Location)},
 * {@code getDestroySpeed(ItemStack, boolean)} and
 * {@code isRandomlyTicked()}.
 */
@Mixin(CraftBlockData.class)
public abstract class CraftBlockDataApiMixin {

    @Shadow
    public abstract net.minecraft.world.level.block.state.BlockState getState();

    @Unique
    public org.bukkit.util.VoxelShape getCollisionShape(Location location) {
        Preconditions.checkArgument(location != null, "location must not be null");
        org.bukkit.craftbukkit.v.CraftWorld world = (org.bukkit.craftbukkit.v.CraftWorld) location.getWorld();
        Preconditions.checkArgument(world != null, "location must not have a null world");
        // Paper: CraftLocation.toBlockPosition(location)
        BlockPos position = org.bukkit.craftbukkit.v.util.CraftLocation.toBlockPosition(location);
        VoxelShape shape = this.getState().getCollisionShape(world.getHandle(), position);
        return new org.bukkit.craftbukkit.v.util.CraftVoxelShape(shape);
    }

    @Unique
    public float getDestroySpeed(final org.bukkit.inventory.ItemStack itemStack, final boolean considerEnchants) {
        // Paper 1.20.1 Add-Destroy-Speed-API.patch 原文；deobf 无 unwrap，用等价 asNMSCopy
        net.minecraft.world.item.ItemStack nmsItemStack = org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(itemStack);
        float speed = nmsItemStack.getDestroySpeed(this.getState());
        if (speed > 1.0F && considerEnchants) {
            int enchantLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY, nmsItemStack);
            if (enchantLevel > 0) {
                speed += enchantLevel * enchantLevel + 1;
            }
        }
        return speed;
    }

    @Unique
    public boolean isRandomlyTicked() {
        return this.getState().isRandomlyTicking();
    }
}
