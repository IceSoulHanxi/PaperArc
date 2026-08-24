package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.block.CraftLectern;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
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

    @Unique
    private net.minecraft.world.level.LevelAccessor getWorldHandle() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getWorldHandle();
    }

    @Unique
    private net.minecraft.core.BlockPos getPosition() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getPosition();
    }

    @Unique
    public boolean hasBook() {
        return this.getWorldHandle().getBlockEntity(this.getPosition()) instanceof LecternBlockEntity lectern && lectern.hasBook();
    }
}
