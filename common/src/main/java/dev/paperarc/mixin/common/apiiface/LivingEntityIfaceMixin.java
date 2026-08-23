package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.LivingEntity} (generated).
 * Adds 46 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.LivingEntity", remap = false)
public interface LivingEntityIfaceMixin {

    public abstract org.bukkit.block.Block getTargetBlock(int p0, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode p1);

    public abstract org.bukkit.block.BlockFace getTargetBlockFace(int p0, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode p1);

    public abstract com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int p0, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode p1);

    public abstract org.bukkit.entity.Entity getTargetEntity(int p0, boolean p1);

    public abstract com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int p0, boolean p1);

    public abstract org.bukkit.util.RayTraceResult rayTraceEntities(int p0, boolean p1);

    public abstract void setArrowsInBody(int p0, boolean p1);

    public abstract void setNextArrowRemoval(int p0);

    public abstract int getNextArrowRemoval();

    public abstract int getBeeStingerCooldown();

    public abstract void setBeeStingerCooldown(int p0);

    public abstract int getBeeStingersInBody();

    public abstract void setBeeStingersInBody(int p0);

    public abstract void setNextBeeStingerRemoval(int p0);

    public abstract int getNextBeeStingerRemoval();

    public abstract void setKiller(org.bukkit.entity.Player p0);

    public abstract boolean clearActivePotionEffects();

    public abstract boolean hasLineOfSight(org.bukkit.Location p0);

    public abstract int getArrowsStuck();

    public abstract void setArrowsStuck(int p0);

    public abstract int getShieldBlockingDelay();

    public abstract void setShieldBlockingDelay(int p0);

    public abstract float getSidewaysMovement();

    public abstract float getUpwardsMovement();

    public abstract float getForwardsMovement();

    public abstract void startUsingItem(org.bukkit.inventory.EquipmentSlot p0);

    public abstract void completeUsingActiveItem();

    public abstract org.bukkit.inventory.ItemStack getActiveItem();

    public abstract void clearActiveItem();

    public abstract int getActiveItemRemainingTime();

    public abstract void setActiveItemRemainingTime(int p0);

    public abstract boolean hasActiveItem();

    public abstract int getActiveItemUsedTime();

    public abstract org.bukkit.inventory.EquipmentSlot getActiveItemHand();

    public abstract boolean isJumping();

    public abstract void setJumping(boolean p0);

    public abstract void playPickupItemAnimation(org.bukkit.entity.Item p0, int p1);

    public abstract float getHurtDirection();

    public abstract void setHurtDirection(float p0);

    public abstract void knockback(double p0, double p1, double p2);

    public abstract void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot p0);

    public abstract void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot p0, java.util.Collection p1);

    public abstract org.bukkit.inventory.ItemStack damageItemStack(org.bukkit.inventory.ItemStack p0, int p1);

    public abstract float getBodyYaw();

    public abstract void setBodyYaw(float p0);

    public abstract boolean canUseEquipmentSlot(org.bukkit.inventory.EquipmentSlot p0);
}
