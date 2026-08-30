package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftEntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's per-slot drop chance API on CraftEntityEquipment.
 *
 * Paper delegates to {@code Mob#getEquipmentDropChance(slot)} which is protected
 * in vanilla 1.20.1 (publicized by Paper's AT); here it is widened via AT
 * (m_21519_(Lnet/minecraft/world/entity/EquipmentSlot;)F) and called directly.
 * {@code Mob#setDropChance} is public and called directly.
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
        return mob.getEquipmentDropChance(nms);
    }

    @Unique
    public void setDropChance(EquipmentSlot slot, float chance) {
        Preconditions.checkArgument(entity.getHandle() instanceof net.minecraft.world.entity.Mob,
                "Cannot set drop chance for non-Mob entity");
        ((net.minecraft.world.entity.Mob) entity.getHandle())
                .setDropChance(CraftEquipmentSlot.getNMS(slot), chance);
    }
}
