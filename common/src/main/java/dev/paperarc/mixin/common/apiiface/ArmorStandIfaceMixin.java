package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.ArmorStand} (generated).
 * Adds 20 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.ArmorStand", remap = false)
public interface ArmorStandIfaceMixin {

    public abstract boolean canMove();

    public abstract void setCanMove(boolean p0);

    public abstract boolean canTick();

    public abstract void setCanTick(boolean p0);

    public abstract org.bukkit.inventory.ItemStack getItem(org.bukkit.inventory.EquipmentSlot p0);

    public abstract void setItem(org.bukkit.inventory.EquipmentSlot p0, org.bukkit.inventory.ItemStack p1);

    public abstract java.util.Set getDisabledSlots();

    public abstract boolean isSlotDisabled(org.bukkit.inventory.EquipmentSlot p0);

    public abstract io.papermc.paper.math.Rotations getBodyRotations();

    public abstract void setBodyRotations(io.papermc.paper.math.Rotations p0);

    public abstract io.papermc.paper.math.Rotations getLeftArmRotations();

    public abstract void setLeftArmRotations(io.papermc.paper.math.Rotations p0);

    public abstract io.papermc.paper.math.Rotations getRightArmRotations();

    public abstract void setRightArmRotations(io.papermc.paper.math.Rotations p0);

    public abstract io.papermc.paper.math.Rotations getLeftLegRotations();

    public abstract void setLeftLegRotations(io.papermc.paper.math.Rotations p0);

    public abstract io.papermc.paper.math.Rotations getRightLegRotations();

    public abstract void setRightLegRotations(io.papermc.paper.math.Rotations p0);

    public abstract io.papermc.paper.math.Rotations getHeadRotations();

    public abstract void setHeadRotations(io.papermc.paper.math.Rotations p0);
}
