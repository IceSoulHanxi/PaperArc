package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.google.common.base.Preconditions;
import io.papermc.paper.entity.Frictional;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attributable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.LivingEntity} (generated).
 * Adds 46 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.LivingEntity", remap = false)
public interface LivingEntityIfaceMixin extends io.papermc.paper.entity.Frictional {

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
    public abstract void setNextArrowRemoval(int p0);

    @Unique
    public abstract int getNextArrowRemoval();

    @Unique
    public abstract int getBeeStingerCooldown();

    @Unique
    public abstract void setBeeStingerCooldown(int p0);

    @Unique
    public abstract int getBeeStingersInBody();

    @Unique
    public abstract void setBeeStingersInBody(int p0);

    @Unique
    public abstract void setNextBeeStingerRemoval(int p0);

    @Unique
    public abstract int getNextBeeStingerRemoval();

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
    public abstract float getSidewaysMovement();

    @Unique
    public abstract float getUpwardsMovement();

    @Unique
    public abstract float getForwardsMovement();

    @Unique
    public abstract void startUsingItem(org.bukkit.inventory.EquipmentSlot p0);

    @Unique
    public abstract void completeUsingActiveItem();

    @Unique
    public abstract org.bukkit.inventory.ItemStack getActiveItem();

    @Unique
    public abstract void clearActiveItem();

    @Unique
    public abstract int getActiveItemRemainingTime();

    @Unique
    public abstract void setActiveItemRemainingTime(int p0);

    @Unique
    public abstract boolean hasActiveItem();

    @Unique
    public abstract int getActiveItemUsedTime();

    @Unique
    public abstract org.bukkit.inventory.EquipmentSlot getActiveItemHand();

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
    public abstract boolean canUseEquipmentSlot(org.bukkit.inventory.EquipmentSlot p0);

    @Unique
    public abstract org.bukkit.inventory.ItemStack damageItemStack(org.bukkit.inventory.ItemStack p0, int p1);

    @Unique
    public abstract org.bukkit.block.BlockFace getTargetBlockFace(int p0, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode p1);

    /**
     * paper {@code Frictional}（B3-2）：NOT_SET 时保持 vanilla 行为。
     * 状态存 NMS 注入字段，生效点见 {@code entity.LivingEntityFrictionMixin} /
     * {@code entity.ItemEntityFrictionMixin}。
     */
    @Unique
    public default net.kyori.adventure.util.TriState getFrictionState() {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.getFrictionState(this);
    }

    @Unique
    public default void setFrictionState(net.kyori.adventure.util.TriState state) {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.setFrictionState(this, state);
    }

    @Unique
    public default Block getTargetBlock(int maxDistance) {
        LivingEntity self = (LivingEntity) this;
        return self.getTargetBlock(maxDistance, TargetBlockInfo.FluidMode.NEVER);
    }

    @Unique
    public default BlockFace getTargetBlockFace(int maxDistance) {
        LivingEntity self = (LivingEntity) this;
        return self.getTargetBlockFace(maxDistance, FluidCollisionMode.NEVER);
    }

    @Unique
    public default TargetBlockInfo getTargetBlockInfo(int maxDistance) {
        LivingEntity self = (LivingEntity) this;
        return self.getTargetBlockInfo(maxDistance, TargetBlockInfo.FluidMode.NEVER);
    }

    @Unique
    public default Entity getTargetEntity(int maxDistance) {
        LivingEntity self = (LivingEntity) this;
        return self.getTargetEntity(maxDistance, false);
    }

    @Unique
    public default TargetEntityInfo getTargetEntityInfo(int maxDistance) {
        LivingEntity self = (LivingEntity) this;
        return self.getTargetEntityInfo(maxDistance, false);
    }

    @Unique
    public default RayTraceResult rayTraceEntities(int maxDistance) {
        LivingEntity self = (LivingEntity) this;
        return self.rayTraceEntities(maxDistance, false);
    }

    @Unique
    public default int getItemUseRemainingTime() {
        LivingEntity self = (LivingEntity) this;
        return self.getActiveItemRemainingTime();
    }

    @Unique
    public default int getHandRaisedTime() {
        LivingEntity self = (LivingEntity) this;
        return self.getActiveItemUsedTime();
    }

    @Unique
    public default boolean isHandRaised() {
        LivingEntity self = (LivingEntity) this;
        return self.hasActiveItem();
    }

    @Unique
    public default EquipmentSlot getHandRaised() {
        LivingEntity self = (LivingEntity) this;
        return self.getActiveItemHand();
    }

    @Unique
    public default void playPickupItemAnimation(Item item) {
        LivingEntity self = (LivingEntity) this;
        self.playPickupItemAnimation(item, item.getItemStack().getAmount());
    }

    @Unique
    public default void swingHand(EquipmentSlot hand) {
        LivingEntity self = (LivingEntity) this;
        Preconditions.checkArgument(hand != null && hand.isHand(), String.format("Expected a valid hand, got \"%s\" instead!", hand));
        if (hand == EquipmentSlot.HAND) {
            self.swingMainHand();
        } else {
            self.swingOffHand();
        }

    }
}
