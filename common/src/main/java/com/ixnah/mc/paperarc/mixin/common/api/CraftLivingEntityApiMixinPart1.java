package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Method;

import com.destroystokyo.paper.block.TargetBlockInfo;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Adds Paper's More-LivingEntity-API additions to CraftLivingEntity (batch B28, part 1
 * of the alphabetical slice: broadcastSlotBreak .. getActiveItemUsedTime).
 *
 * Paper refs: patches/server/More-LivingEntity-API.patch.
 *
 * Mapping notes vs Paper source:
 * - {@code LivingEntity#completeUsingItem()} is protected in vanilla mojmap ->
 *   reflective access (cached).
 * - {@code CraftItemStack#getHandle()} is package-private in CraftBukkit ->
 *   reflective access (cached).
 * - Paper's player-filtered {@code broadcastSlotBreak(EquipmentSlot, Collection)}
 *   has no vanilla counterpart; falls back to the global slot-break broadcast.
 * - CB's Cause-aware {@code removeAllEffects(Cause)} overload is not visible in the
 *   compile-time mojmap jar; vanilla's no-arg {@code removeAllEffects()} is used.
 */
@Mixin(CraftLivingEntity.class)
public abstract class CraftLivingEntityApiMixinPart1 {

    @Shadow
    public abstract LivingEntity getHandle();

    @Unique
    public void broadcastSlotBreak(EquipmentSlot slot) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        LivingEntity handle = this.getHandle();
        net.minecraft.world.item.ItemStack stack = handle.getItemBySlot(nmsSlot);
        // 1.20.1 has no LivingEntity#onEquippedItemBroken (1.21+); damage the stack by 1
        // via ItemStack#hurtAndBreak(int, T, Consumer<T>) to trigger the break broadcast.
        stack.hurtAndBreak(1, handle, (ignored) -> {});
    }

    @Unique
    public void broadcastSlotBreak(EquipmentSlot slot, java.util.Collection<Player> players) {
        // Paper sends the break animation only to the given players; vanilla exposes no
        // player-filtered broadcast hook, so fall back to the global slot-break broadcast.
        this.broadcastSlotBreak(slot);
    }

    @Unique
    public boolean canUseEquipmentSlot(EquipmentSlot slot) {
        // 1.20.1 LivingEntity has no canUseSlot (1.21+); accept all slots.
        return true;
    }

    @Unique
    public void clearActiveItem() {
        this.getHandle().stopUsingItem();
    }

    @Unique
    public boolean clearActivePotionEffects() {
        // CB's Cause-aware removeAllEffects(Cause) overload is not visible in the
        // compile-time mojmap jar; vanilla's no-arg form has the same clearing behaviour.
        return this.getHandle().removeAllEffects();
    }

    @Unique
    public void completeUsingActiveItem() {
        // LivingEntity#completeUsingItem 由 AT 加宽（m_8095_）后直访
        this.getHandle().completeUsingItem();
    }

    @Unique
    public void damageItemStack(EquipmentSlot slot, int amount) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        this.getHandle().getItemBySlot(nmsSlot).hurtAndBreak(amount, this.getHandle(), (ignored) -> {});
    }

    @Unique
    public ItemStack damageItemStack(ItemStack stack, int amount) {
        if (!(stack instanceof CraftItemStack craftStack)) {
            return stack;
        }
        // CraftItemStack#handle 是包私有字段、无公有 getHandle；asNMSCopy 对
        // CraftItemStack 直接返回内部 handle，零反射。
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(craftStack);
        nmsStack.hurtAndBreak(amount, this.getHandle(), (ignored) -> {});
        return craftStack;
    }

    @Unique
    public ItemStack getActiveItem() {
        return CraftItemStack.asCraftMirror(this.getHandle().getUseItem());
    }

    @Unique
    public EquipmentSlot getActiveItemHand() {
        return CraftEquipmentSlot.getHand(this.getHandle().getUsedItemHand());
    }

    @Unique
    public int getActiveItemRemainingTime() {
        return this.getHandle().getUseItemRemainingTicks();
    }

    @Unique
    public int getActiveItemUsedTime() {
        return this.getHandle().getTicksUsingItem();
    }

    @Unique
    public int getArrowsStuck() {
        return this.getHandle().getArrowCount();
    }

    @Unique
    public int getBeeStingerCooldown() {
        // Same vanilla timer Paper maps this getter onto: public field removeStingerTime.
        return this.getHandle().removeStingerTime;
    }

    @Unique
    public int getBeeStingersInBody() {
        return this.getHandle().getStingerCount();
    }

    @Unique
    public float getBodyYaw() {
        return this.getHandle().yBodyRot;
    }

    @Unique
    public float getForwardsMovement() {
        return this.getHandle().zza;
    }

    @Unique
    public float getHurtDirection() {
        return this.getHandle().getHurtDir();
    }

    @Unique
    public int getNextArrowRemoval() {
        return this.getHandle().removeArrowTime;
    }

    @Unique
    public int getNextBeeStingerRemoval() {
        return this.getHandle().removeStingerTime;
    }

    @Unique
    public int getShieldBlockingDelay() {
        // Paper-added state; vanilla NMS has no field -> ApiState side map, default 5.
        return com.ixnah.mc.paperarc.bridge.ApiState.get(this, "shieldBlockingDelay", 5);
    }

    @Unique
    public float getSidewaysMovement() {
        return this.getHandle().xxa;
    }

    @Unique
    private BlockHitResult paperarc$rayTraceTarget(int maxDistance, TargetBlockInfo.FluidMode fluidMode) {
        LivingEntity handle = this.getHandle();
        Vec3 start = new Vec3(handle.getX(), handle.getEyeY(), handle.getZ());
        Vec3 view = handle.getViewVector(1.0F);
        Vec3 end = start.add(view.x * maxDistance, view.y * maxDistance, view.z * maxDistance);
        ClipContext.Fluid fluid = switch (fluidMode) {
            case NEVER -> ClipContext.Fluid.NONE;
            case SOURCE_ONLY -> ClipContext.Fluid.SOURCE_ONLY;
            case ALWAYS -> ClipContext.Fluid.ANY;
        };
        return handle.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, fluid, handle));
    }

    @Unique
    public org.bukkit.block.Block getTargetBlock(int maxDistance, TargetBlockInfo.FluidMode fluidMode) {
        BlockHitResult hit = this.paperarc$rayTraceTarget(maxDistance, fluidMode);
        if (hit.getType() == HitResult.Type.MISS) {
            return null;
        }
        return CraftBlock.at(this.getHandle().level(), hit.getBlockPos());
    }

    @Unique
    public org.bukkit.block.BlockFace getTargetBlockFace(int maxDistance, TargetBlockInfo.FluidMode fluidMode) {
        BlockHitResult hit = this.paperarc$rayTraceTarget(maxDistance, fluidMode);
        if (hit.getType() == HitResult.Type.MISS) {
            return null;
        }
        return CraftBlock.notchToBlockFace(hit.getDirection());
    }
}
