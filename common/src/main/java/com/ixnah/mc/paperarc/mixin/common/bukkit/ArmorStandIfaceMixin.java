package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.ArmorStand} (generated).
 * Adds 20 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.ArmorStand", remap = false)
public interface ArmorStandIfaceMixin {

    @Unique
    public abstract boolean canMove();

    @Unique
    public abstract void setCanMove(boolean p0);

    @Unique
    public abstract boolean canTick();

    @Unique
    public abstract void setCanTick(boolean p0);

    @Unique
    public abstract org.bukkit.inventory.ItemStack getItem(org.bukkit.inventory.EquipmentSlot p0);

    @Unique
    public abstract void setItem(org.bukkit.inventory.EquipmentSlot p0, org.bukkit.inventory.ItemStack p1);

    @Unique
    public abstract java.util.Set getDisabledSlots();

    @Unique
    public abstract boolean isSlotDisabled(org.bukkit.inventory.EquipmentSlot p0);

    @Unique
    public abstract io.papermc.paper.math.Rotations getBodyRotations();

    @Unique
    public abstract void setBodyRotations(io.papermc.paper.math.Rotations p0);

    @Unique
    public abstract io.papermc.paper.math.Rotations getLeftArmRotations();

    @Unique
    public abstract void setLeftArmRotations(io.papermc.paper.math.Rotations p0);

    @Unique
    public abstract io.papermc.paper.math.Rotations getRightArmRotations();

    @Unique
    public abstract void setRightArmRotations(io.papermc.paper.math.Rotations p0);

    @Unique
    public abstract io.papermc.paper.math.Rotations getLeftLegRotations();

    @Unique
    public abstract void setLeftLegRotations(io.papermc.paper.math.Rotations p0);

    @Unique
    public abstract io.papermc.paper.math.Rotations getRightLegRotations();

    @Unique
    public abstract void setRightLegRotations(io.papermc.paper.math.Rotations p0);

    @Unique
    public abstract io.papermc.paper.math.Rotations getHeadRotations();

    @Unique
    public abstract void setHeadRotations(io.papermc.paper.math.Rotations p0);
}
