package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.LivingEntity} (generated, trimmed for 1.20.1).
 * Adds 31 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.LivingEntity", remap = false)
public interface LivingEntityIfaceMixin {

    @Unique
    public abstract org.bukkit.block.Block getTargetBlock(int p0, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode p1);

    @Unique
    public abstract org.bukkit.block.BlockFace getTargetBlockFace(int p0, org.bukkit.FluidCollisionMode p1);

    @Unique
    public abstract com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int p0, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode p1);

    @Unique
    public abstract org.bukkit.entity.Entity getTargetEntity(int p0, boolean p1);

    @Unique
    public abstract com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int p0, boolean p1);

    @Unique
    public abstract org.bukkit.util.RayTraceResult rayTraceEntities(int p0, boolean p1);

    @Unique
    public abstract void setArrowsInBody(int p0, boolean p1);

    @Unique
    public abstract int getBeeStingerCooldown();

    @Unique
    public abstract void setBeeStingerCooldown(int p0);

    @Unique
    public abstract int getBeeStingersInBody();

    @Unique
    public abstract void setBeeStingersInBody(int p0);

    @Unique
    public abstract void setKiller(org.bukkit.entity.Player p0);

    @Unique
    public abstract boolean clearActivePotionEffects();

    @Unique
    public abstract boolean hasLineOfSight(org.bukkit.Location p0);

    @Unique
    public abstract int getArrowsStuck();

    @Unique
    public abstract void setArrowsStuck(int p0);

    @Unique
    public abstract int getShieldBlockingDelay();

    @Unique
    public abstract void setShieldBlockingDelay(int p0);

    @Unique
    public abstract org.bukkit.inventory.ItemStack getActiveItem();

    @Unique
    public abstract void clearActiveItem();

    @Unique
    public abstract boolean isJumping();

    @Unique
    public abstract void setJumping(boolean p0);

    @Unique
    public abstract void playPickupItemAnimation(org.bukkit.entity.Item p0, int p1);

    @Unique
    public abstract float getHurtDirection();

    @Unique
    public abstract void setHurtDirection(float p0);

    @Unique
    public abstract void knockback(double p0, double p1, double p2);

    @Unique
    public abstract void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot p0);

    @Unique
    public abstract void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot p0, java.util.Collection p1);

    @Unique
    public abstract void damageItemStack(org.bukkit.inventory.EquipmentSlot p0, int p1);

    @Unique
    public abstract float getBodyYaw();

    @Unique
    public abstract void setBodyYaw(float p0);
    @Unique
    public abstract int getItemUseRemainingTime();
    @Unique
    public abstract int getHandRaisedTime();
    @Unique
    public abstract boolean isHandRaised();
    @Unique
    public abstract org.bukkit.inventory.EquipmentSlot getHandRaised();
}
