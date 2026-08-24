package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Method;

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
 * Paper delegates to {@code Mob#getEquipmentDropChance(slot)} which is protected
 * in vanilla 1.21.1 (publicized by Paper's AT); a Craft-host mixin cannot widen
 * that, so the getter is invoked reflectively. {@code Mob#setDropChance} is
 * public and called directly.
 */
@Mixin(CraftEntityEquipment.class)
public abstract class CraftEntityEquipmentApiMixin {

    @Unique
    private static volatile Method PAPERARC$GET_EQUIPMENT_DROP_CHANCE;

    @Shadow
    @Final
    private CraftLivingEntity entity;

    @Unique
    private static Method paperarc$getEquipmentDropChanceMethod() throws NoSuchMethodException {
        Method m = PAPERARC$GET_EQUIPMENT_DROP_CHANCE;
        if (m == null) {
            synchronized (CraftEntityEquipmentApiMixin.class) {
                if (PAPERARC$GET_EQUIPMENT_DROP_CHANCE == null) {
                    Method resolved = net.minecraft.world.entity.Mob.class
                            .getDeclaredMethod("getEquipmentDropChance", net.minecraft.world.entity.EquipmentSlot.class);
                    resolved.setAccessible(true);
                    PAPERARC$GET_EQUIPMENT_DROP_CHANCE = resolved;
                }
                m = PAPERARC$GET_EQUIPMENT_DROP_CHANCE;
            }
        }
        return m;
    }

    @Unique
    public float getDropChance(EquipmentSlot slot) {
        net.minecraft.world.entity.EquipmentSlot nms = CraftEquipmentSlot.getNMS(slot);
        if (!(entity.getHandle() instanceof net.minecraft.world.entity.Mob mob)) {
            return 1;
        }
        try {
            return (Float) paperarc$getEquipmentDropChanceMethod().invoke(mob, nms);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Mob.getEquipmentDropChance not found", e);
        }
    }

    @Unique
    public void setDropChance(EquipmentSlot slot, float chance) {
        Preconditions.checkArgument(entity.getHandle() instanceof net.minecraft.world.entity.Mob,
                "Cannot set drop chance for non-Mob entity");
        ((net.minecraft.world.entity.Mob) entity.getHandle())
                .setDropChance(CraftEquipmentSlot.getNMS(slot), chance);
    }
}
