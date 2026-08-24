package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.block.impl.CraftBed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Support mixin for {@code Add-missing-block-data-API}: Paper adds
 * {@code set(OCCUPIED, occupied)} to the block-data impl of beds. The spigot
 * base only ships {@code isOccupied()}, so the setter is implemented here over
 * the vanilla {@code BedBlock.OCCUPIED} property.
 */
@Mixin(CraftBed.class)
public abstract class CraftBedBlockDataApiMixin {

    @Unique
    private static final BooleanProperty PAPERARC$OCCUPIED = BedBlock.OCCUPIED;

    @Unique
    public void setOccupied(boolean occupied) {
        ((CraftBlockData) (Object) this).set(PAPERARC$OCCUPIED, occupied);
    }
}
