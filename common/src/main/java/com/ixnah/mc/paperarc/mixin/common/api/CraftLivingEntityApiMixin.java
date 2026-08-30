package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import java.lang.reflect.Method;import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftRayTraceResult;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper API 方法补齐（合并原 Part1/Part2）：org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity。
 * 因跨方法共享补充字段（shieldBlockingDelay 等），两 Part 已合并为单一 mixin 类。
 */
@Mixin(CraftLivingEntity.class)
public abstract class CraftLivingEntityApiMixin {

    // ---- Paper 补充字段（注入 CraftLivingEntity，字段名对齐 Paper patch 无前缀）----

    @Unique
    private int shieldBlockingDelay = 5;

    @Unique
    private float upwardsMovement;

    @Unique
    private float hurtDirection;


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
        // Paper-added state; vanilla NMS has no field -> injected Craft field, default 5.
        return this.shieldBlockingDelay;
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


    @Shadow
    public abstract RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode);

    /**
     * 0. BlockFace getTargetBlockFace(int, FluidCollisionMode)
     */
    @Unique
    public BlockFace getTargetBlockFace(int maxDistance, FluidCollisionMode fluidCollisionMode) {
        RayTraceResult hit = this.rayTraceBlocks((double) maxDistance, fluidCollisionMode);
        return hit == null ? null : hit.getHitBlockFace();
    }

    /**
     * 1. TargetBlockInfo getTargetBlockInfo(int, TargetBlockInfo.FluidMode)
     */
    @Unique
    public TargetBlockInfo getTargetBlockInfo(int maxDistance, TargetBlockInfo.FluidMode fluidMode) {
        FluidCollisionMode collisionMode;
        if (fluidMode == TargetBlockInfo.FluidMode.ALWAYS) {
            collisionMode = FluidCollisionMode.ALWAYS;
        } else if (fluidMode == TargetBlockInfo.FluidMode.SOURCE_ONLY) {
            collisionMode = FluidCollisionMode.SOURCE_ONLY;
        } else {
            collisionMode = FluidCollisionMode.NEVER;
        }
        RayTraceResult hit = this.rayTraceBlocks((double) maxDistance, collisionMode);
        if (hit == null || hit.getHitBlock() == null || hit.getHitBlockFace() == null) {
            return null;
        }
        return new TargetBlockInfo(hit.getHitBlock(), hit.getHitBlockFace());
    }

    /** Shared NMS ray trace used by getTargetEntity / getTargetEntityInfo / rayTraceEntities. */
    @Unique
    private RayTraceResult paperarc$rayTraceEntities0(int maxDistance, boolean ignorePassable) {
        LivingEntity handle = this.getHandle();
        if (maxDistance < 1.0E7 || maxDistance <= 0.0D || maxDistance > 2.147483647E9D) {
            throw new IllegalArgumentException("maxDistance must be positive");
        }
        Level level = handle.level();
        Vec3 from = handle.getEyePosition();
        Vec3 dir = Vec3.directionFromRotation(handle.getXRot(), handle.getYRot()).scale(maxDistance);
        Vec3 to = from.add(dir);
        AABB searchBox = handle.getBoundingBox().expandTowards(dir).inflate(1.0D);
        net.minecraft.world.phys.EntityHitResult nmsHit = ProjectileUtil.getEntityHitResult(level, handle, from, to,
                searchBox,
                (net.minecraft.world.entity.Entity e) -> !e.isSpectator() && e.isPickable() && e.isAlive());
        // ignorePassable 无法精确映射（NMS 无对应参数），仅按可拾取碰撞体过滤
        if (nmsHit == null) {
            return null;
        }
        org.bukkit.World world = ((CraftEntity) (Object) this).getWorld();
        return CraftRayTraceResult.fromNMS(world, nmsHit);
    }

    /**
     * 2. Entity getTargetEntity(int, boolean)
     */
    @Unique
    public org.bukkit.entity.Entity getTargetEntity(int maxDistance, boolean ignorePassable) {
        RayTraceResult hit = this.paperarc$rayTraceEntities0(maxDistance, ignorePassable);
        return hit == null ? null : hit.getHitEntity();
    }

    /**
     * 3. TargetEntityInfo getTargetEntityInfo(int, boolean)
     */
    @Unique
    public TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignorePassable) {
        RayTraceResult hit = this.paperarc$rayTraceEntities0(maxDistance, ignorePassable);
        if (hit == null || hit.getHitEntity() == null) {
            return null;
        }
        return new TargetEntityInfo(hit.getHitEntity(), hit.getHitPosition());
    }

    /**
     * 4. float getUpwardsMovement()
     */
    @Unique
    public float getUpwardsMovement() {
        // vanilla 无对应存储字段，Paper 自有状态 → 注入 Craft 字段，默认 0.0f
        return this.upwardsMovement;
    }

    /**
     * 5. boolean hasActiveItem()
     */
    @Unique
    public boolean hasActiveItem() {
        return this.getHandle().isUsingItem();
    }

    /**
     * 6. boolean hasLineOfSight(Location)
     */
    @Unique
    public boolean hasLineOfSight(Location location) {
        LivingEntity handle = this.getHandle();
        Vec3 eye = handle.getEyePosition();
        Vec3 target = new Vec3(location.getX(), location.getY(), location.getZ());
        if (eye.distanceToSqr(target) < 1.0E-7D) {
            return true;
        }
        ClipContext ctx = new ClipContext(eye, target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, handle);
        return handle.level().clip(ctx).getType() == HitResult.Type.MISS;
    }

    /**
     * Hand-Raised / Item-Use API (LivingEntity-Hand-Raised-Item-Use-API.patch).
     */
    @Unique
    public int getItemUseRemainingTime() {
        return this.getHandle().getUseItemRemainingTicks();
    }

    @Unique
    public int getHandRaisedTime() {
        return this.getHandle().getTicksUsingItem();
    }

    @Unique
    public boolean isHandRaised() {
        return this.getHandle().isUsingItem();
    }

    @Unique
    public EquipmentSlot getHandRaised() {
        return this.getHandle().getUsedItemHand() == InteractionHand.MAIN_HAND
                ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
    }

    /**
     * 7. boolean isJumping()
     */
    @Unique
    public boolean isJumping() {
        return this.getHandle().jumping;
    }

    /**
     * 8. void knockback(double, double, double)
     */
    @Unique
    public void knockback(double strength, double x, double z) {
        this.getHandle().knockback(strength, x, z);
    }

    /**
     * 9. void playPickupItemAnimation(Item, int)
     */
    @Unique
    public void playPickupItemAnimation(Item item, int quantity) {
        if (item == null) {
            return;
        }
        this.getHandle().take(((CraftItem) item).getHandle(), quantity);
    }

    /**
     * 10. RayTraceResult rayTraceEntities(int, boolean)
     */
    @Unique
    public RayTraceResult rayTraceEntities(int maxDistance, boolean ignorePassable) {
        return this.paperarc$rayTraceEntities0(maxDistance, ignorePassable);
    }

    /**
     * 11. void setActiveItemRemainingTime(int)
     */
    @Unique
    public void setActiveItemRemainingTime(int ticks) {
        this.getHandle().useItemRemaining = ticks;
    }

    /**
     * 12. void setArrowsInBody(int, boolean)
     */
    @Unique
    public void setArrowsInBody(int count, boolean fireEvent) {
        // ArrowBodyCountChangeEvent 需 Paper 事件基建（CraftEventFactory 补丁），Arclight spigot 构建无此设施：
        // fireEvent=true 时事件不触发，仅同步数量
        this.getHandle().setArrowCount(count);
    }

    /**
     * 13. void setArrowsStuck(int)
     * @deprecated legacy alias of bee stingers in vanilla storage
     */
    @Unique
    public void setArrowsStuck(int count) {
        this.getHandle().setStingerCount(count);
    }

    /**
     * 14. void setBeeStingerCooldown(int)
     */
    @Unique
    public void setBeeStingerCooldown(int ticks) {
        // Paper: BeeStingerCooldown 与 NextBeeStingerRemoval 同源（removeStingerTime）
        this.getHandle().removeStingerTime = ticks;
    }

    /**
     * 15. void setBeeStingersInBody(int)
     */
    @Unique
    public void setBeeStingersInBody(int count) {
        this.getHandle().setStingerCount(count);
    }

    /**
     * 16. void setBodyYaw(float)
     */
    @Unique
    public void setBodyYaw(float degrees) {
        this.getHandle().yBodyRot = degrees;
    }

    /**
     * 17. void setHurtDirection(float)
     */
    @Unique
    public void setHurtDirection(float direction) {
        // vanilla 1.20.1 无公开 hurtDir setter（仅公有 getHurtDir()），Paper 自有状态 → 注入 Craft 字段
        this.hurtDirection = direction;
    }

    /**
     * 18. void setJumping(boolean)
     */
    @Unique
    public void setJumping(boolean jumping) {
        this.getHandle().jumping = jumping;
    }

    /**
     * 19. void setKiller(Player)
     */
    @Unique
    public void setKiller(Player killer) {
        LivingEntity handle = this.getHandle();
        if (killer == null) {
            handle.lastHurtByPlayer = null;
            return;
        }
        handle.lastHurtByPlayer =
                ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer) killer).getHandle();
    }

    /**
     * 20. void setNextArrowRemoval(int)
     */
    @Unique
    public void setNextArrowRemoval(int ticks) {
        this.getHandle().removeArrowTime = ticks;
    }

    /**
     * 21. void setNextBeeStingerRemoval(int)
     */
    @Unique
    public void setNextBeeStingerRemoval(int ticks) {
        this.getHandle().removeStingerTime = ticks;
    }

    /**
     * 22. void setShieldBlockingDelay(int)
     */
    @Unique
    public void setShieldBlockingDelay(int delay) {
        // vanilla 无 shieldBlockingDelay 存储（Paper 自定义字段）→ 注入 Craft 字段，默认 5
        this.shieldBlockingDelay = delay;
    }

    /**
     * 23. void startUsingItem(EquipmentSlot)
     */
    @Unique
    public void startUsingItem(EquipmentSlot hand) {
        InteractionHand nmsHand;
        if (hand == EquipmentSlot.HAND) {
            nmsHand = InteractionHand.MAIN_HAND;
        } else if (hand == EquipmentSlot.OFF_HAND) {
            nmsHand = InteractionHand.OFF_HAND;
        } else {
            throw new IllegalArgumentException("Cannot use an armor slot: " + hand);
        }
        this.getHandle().startUsingItem(nmsHand);
    }
}
