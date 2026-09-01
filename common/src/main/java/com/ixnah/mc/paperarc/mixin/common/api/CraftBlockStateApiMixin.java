package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Port of Paper's Add-getDrops-to-BlockState.patch and the
 * Add-hasCollision-methods-to-various-places.patch addition on
 * {@link CraftBlockState}: {@code BlockState#getDrops(ItemStack, Entity)}
 * and {@code BlockState#isCollidable()}.
 */
@Mixin(CraftBlockState.class)
public abstract class CraftBlockStateApiMixin {

    @Shadow
    protected CraftWorld world;
    @Shadow
    private BlockPos position;
    @Shadow
    protected BlockState data;

    @Shadow
    public abstract BlockData getBlockData();

    @Unique
    public Collection<org.bukkit.inventory.ItemStack> getDrops(org.bukkit.inventory.ItemStack item, org.bukkit.entity.Entity entity) {
        this.paperarc$requirePlaced();
        ItemStack nms = CraftItemStack.asNMSCopy(item);

        // Modelled off EntityHuman#hasBlock, as in Paper's implementation
        if (item == null || !this.data.requiresCorrectToolForDrops() || nms.isCorrectToolForDrops(this.data)) {
            List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                this.data,
                this.world.getHandle(),
                this.position,
                this.world.getHandle().getBlockEntity(this.position),
                entity == null ? null : ((org.bukkit.craftbukkit.v.entity.CraftEntity) entity).getHandle(),
                nms
            );
            return drops.stream().map(CraftItemStack::asBukkitCopy).toList();
        } else {
            return Collections.emptyList();
        }
    }

    @Unique
    public boolean isCollidable() {
        return this.paperarc$hasCollision(this.data.getBlock());
    }

    /**
     * Paper reads {@code Block#hasCollision} through an access transformer.
     * The field is widened via AT (f_60443_) and read directly — no reflection.
     */
    @Unique
    private boolean paperarc$hasCollision(net.minecraft.world.level.block.Block block) {
        return block.hasCollision;
    }

    @Unique
    public boolean isSnapshot() {
        // Paper adds a snapshotDisabled flag (default false -> this IS a snapshot);
        // Arclight's CraftBlockState always works on a detached snapshot copy.
        return true;
    }

    /**
     * Replacement for Paper's {@code CraftBlockState#requirePlaced()} guard,
     * which does not exist in vanilla CraftBukkit.
     */
    @Unique
    private void paperarc$requirePlaced() {
        if (this.world == null) {
            throw new IllegalStateException("Cannot get drops of an unplaced block state");
        }
    }
}
