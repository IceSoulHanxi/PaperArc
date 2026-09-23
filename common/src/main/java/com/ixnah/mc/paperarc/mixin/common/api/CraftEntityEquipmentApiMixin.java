package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.inventory.CraftEntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's per-slot drop chance API on CraftEntityEquipment.
 *
 * Reads go through {@code Mob#getDropChances().byEquipment(slot)} (1.21.5+ moved
 * the per-slot chances into a {@code DropChances} record). {@code Mob#setDropChance} is public.
 */
@Mixin(CraftEntityEquipment.class)
public abstract class CraftEntityEquipmentApiMixin {

    @Shadow
    @Final
    private CraftLivingEntity entity;

    @Unique
    public float getDropChance(EquipmentSlot slot) {
        net.minecraft.world.entity.EquipmentSlot nms = CraftEquipmentSlot.getNMS(slot);
        if (!(entity.getHandle() instanceof net.minecraft.world.entity.Mob mob)) {
            return 1;
        }
        return mob.getDropChances().byEquipment(nms);
    }

    @Unique
    public void setDropChance(EquipmentSlot slot, float chance) {
        Preconditions.checkArgument(entity.getHandle() instanceof net.minecraft.world.entity.Mob,
                "Cannot set drop chance for non-Mob entity");
        ((net.minecraft.world.entity.Mob) entity.getHandle())
                .setDropChance(CraftEquipmentSlot.getNMS(slot), chance);
    }
}
