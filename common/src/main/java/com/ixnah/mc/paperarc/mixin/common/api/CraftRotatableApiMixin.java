package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rotatable;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds the bukkit {@link Rotatable} API to the concrete
 * org.bukkit.craftbukkit.v.block.impl.CraftRotatable host, which upstream only
 * implements Orientable. Mirrors CraftBukkit's abstract block.data.CraftRotatable
 * implementation over the vanilla ROTATION_0_15 property.
 */
@Mixin(org.bukkit.craftbukkit.v.block.impl.CraftRotatable.class)
public abstract class CraftRotatableApiMixin implements Rotatable {

    @Unique
    private static final IntegerProperty PAPERARC$ROTATION = BlockStateProperties.ROTATION_16;

    @Unique
    private CraftBlockData paperarc$self() {
        return (CraftBlockData) (Object) this;
    }

    @Unique
    @Override
    public org.bukkit.block.data.BlockData clone() {
        try {
            return (org.bukkit.block.data.BlockData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Unique
    @Override
    public BlockFace getRotation() {
        int data = paperarc$self().getState().getValue(PAPERARC$ROTATION);
        switch (data) {
            case 0x0: return BlockFace.SOUTH;
            case 0x1: return BlockFace.SOUTH_SOUTH_WEST;
            case 0x2: return BlockFace.SOUTH_WEST;
            case 0x3: return BlockFace.WEST_SOUTH_WEST;
            case 0x4: return BlockFace.WEST;
            case 0x5: return BlockFace.WEST_NORTH_WEST;
            case 0x6: return BlockFace.NORTH_WEST;
            case 0x7: return BlockFace.NORTH_NORTH_WEST;
            case 0x8: return BlockFace.NORTH;
            case 0x9: return BlockFace.NORTH_NORTH_EAST;
            case 0xA: return BlockFace.NORTH_EAST;
            case 0xB: return BlockFace.EAST_NORTH_EAST;
            case 0xC: return BlockFace.EAST;
            case 0xD: return BlockFace.EAST_SOUTH_EAST;
            case 0xE: return BlockFace.SOUTH_EAST;
            case 0xF: return BlockFace.SOUTH_SOUTH_EAST;
            default: throw new IllegalArgumentException("Unknown rotation " + data);
        }
    }

    @Unique
    @Override
    public void setRotation(BlockFace rotation) {
        int data;
        switch (rotation) {
            case SOUTH: data = 0x0; break;
            case SOUTH_SOUTH_WEST: data = 0x1; break;
            case SOUTH_WEST: data = 0x2; break;
            case WEST_SOUTH_WEST: data = 0x3; break;
            case WEST: data = 0x4; break;
            case WEST_NORTH_WEST: data = 0x5; break;
            case NORTH_WEST: data = 0x6; break;
            case NORTH_NORTH_WEST: data = 0x7; break;
            case NORTH: data = 0x8; break;
            case NORTH_NORTH_EAST: data = 0x9; break;
            case NORTH_EAST: data = 0xA; break;
            case EAST_NORTH_EAST: data = 0xB; break;
            case EAST: data = 0xC; break;
            case EAST_SOUTH_EAST: data = 0xD; break;
            case SOUTH_EAST: data = 0xE; break;
            case SOUTH_SOUTH_EAST: data = 0xF; break;
            default: throw new IllegalArgumentException("Invalid rotation " + rotation);
        }
        paperarc$self().set(PAPERARC$ROTATION, data);
    }
}
