package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import com.ixnah.mc.paperarc.bridge.ApiState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftRayTraceResult;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * B29 part 2: paper-api LivingEntity additions on Arclight CraftLivingEntity.
 * Host: {@link org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity}.
 */
@Mixin(org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity.class)
public abstract class CraftLivingEntityApiMixinPart2 {

    // region Shadows

    @Shadow
    public abstract LivingEntity getHandle();

    @Shadow
    public abstract RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode);
    // endregion

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
        // vanilla 无对应存储字段，Paper 自有状态 → side-map，默认 0.0f
        return (Float) ApiState.get(this.getHandle(), "paperarc:upwardsMovement", 0.0F);
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
        // vanilla 1.21.1 已无 hurtDirection 存储（旧版字段被移除）→ side-map，默认 0.0f
        ApiState.put(this.getHandle(), "paperarc:hurtDirection", direction);
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
        // vanilla 无 shieldBlockingDelay 存储（Paper 自定义字段）→ side-map，默认 5
        ApiState.put(this.getHandle(), "paperarc:shieldBlockingDelay", delay);
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
