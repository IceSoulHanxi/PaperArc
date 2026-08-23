package dev.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

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
     * The field is protected in NeoForge mappings and we cannot widen it from
     * a single-target mixin, so read it reflectively (Arclight runs
     * Mojang-mapped at runtime).
     */
    @Unique
    private volatile static Field paperarc$hasCollisionField;

    @Unique
    private boolean paperarc$hasCollision(net.minecraft.world.level.block.Block block) {
        try {
            Field field = paperarc$hasCollisionField;
            if (field == null) {
                field = BlockBehaviour.class.getDeclaredField("hasCollision");
                field.setAccessible(true);
                paperarc$hasCollisionField = field;
            }
            return (Boolean) field.get(block);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to access BlockBehaviour#hasCollision", e);
        }
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
