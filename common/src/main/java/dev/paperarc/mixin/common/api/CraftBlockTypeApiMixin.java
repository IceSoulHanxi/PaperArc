package dev.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.block.CraftBlockType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;

/**
 * Adds hasCollision missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Add-hasCollision-methods-to-various-places.patch
 * (CraftBlockType#hasCollision reads protected BlockBehaviour.hasCollision field,
 *  not accessible here -> approximated via default state collision shape).
 */
@Mixin(CraftBlockType.class)
public abstract class CraftBlockTypeApiMixin {

    @Shadow
    public abstract Block getHandle();

    @Unique
    public boolean hasCollision() {
        return !this.getHandle().defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
    }
}
