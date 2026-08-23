package dev.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.block.CraftLectern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.block.entity.LecternBlockEntity;

/**
 * Adds hasBook missing from Arclight CraftBukkit.
 * No dedicated Paper patch; delegates to NMS LecternBlockEntity#hasBook.
 * getTileEntity() is protected on the parent class -> resolved via
 * public CraftBlockState#getWorldHandle/getPosition instead.
 */
@Mixin(CraftLectern.class)
public abstract class CraftLecternApiMixin {

    @Shadow
    public abstract net.minecraft.world.level.LevelAccessor getWorldHandle();

    @Shadow
    public abstract net.minecraft.core.BlockPos getPosition();

    @Unique
    public boolean hasBook() {
        return this.getWorldHandle().getBlockEntity(this.getPosition()) instanceof LecternBlockEntity lectern && lectern.hasBook();
    }
}
