package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.entity.TargetEntityInfo;
import net.minecraft.world.InteractionHand;
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
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftItem;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.util.CraftRayTraceResult;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Adds Paper's More-LivingEntity-API additions to CraftLivingEntity
 * （原批次 B28 + B29 两个切片，2026-09-17 合并为一个 mixin —— 拆成 Part1/Part2
 * 只是当初为了并行写代码）。
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
public abstract class CraftLivingEntityApiMixin {

    /** Paper 侧补充状态（原 ApiState 副表键 "shieldBlockingDelay"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Integer paperarc$shieldBlockingDelay;

    @Shadow
    public abstract LivingEntity getHandle();

    @Unique
    private static volatile Method PAPERARC$COMPLETE_USING_ITEM_METHOD;

    @Unique
    private static volatile Method PAPERARC$CRAFT_STACK_GET_HANDLE_METHOD;

    @Unique
    private static Method paperarc$method(Class<?> owner, String name, Class<?>... params) {
        try {
            Method resolved = owner.getDeclaredMethod(name, params);
            resolved.setAccessible(true);
            return resolved;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: NMS method not found: " + owner.getName() + "." + name, e);
        }
    }

    @Unique
    public void broadcastSlotBreak(EquipmentSlot slot) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        LivingEntity handle = this.getHandle();
        handle.onEquippedItemBroken(handle.getItemBySlot(nmsSlot).getItem(), nmsSlot);
    }

    @Unique
    public void broadcastSlotBreak(EquipmentSlot slot, java.util.Collection<Player> players) {
        // Paper sends the break animation only to the given players; vanilla exposes no
        // player-filtered broadcast hook, so fall back to the global slot-break broadcast.
        this.broadcastSlotBreak(slot);
    }

    @Unique
    public boolean canUseEquipmentSlot(EquipmentSlot slot) {
        return this.getHandle().canUseSlot(CraftEquipmentSlot.getNMS(slot));
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
        try {
            if (PAPERARC$COMPLETE_USING_ITEM_METHOD == null) {
                synchronized (CraftLivingEntityApiMixin.class) {
                    if (PAPERARC$COMPLETE_USING_ITEM_METHOD == null) {
                        PAPERARC$COMPLETE_USING_ITEM_METHOD = paperarc$method(LivingEntity.class, "completeUsingItem");
                    }
                }
            }
            PAPERARC$COMPLETE_USING_ITEM_METHOD.invoke(this.getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: failed to complete using active item", e);
        }
    }

    @Unique
    public void damageItemStack(EquipmentSlot slot, int amount) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        this.getHandle().getItemBySlot(nmsSlot).hurtAndBreak(amount, this.getHandle(), nmsSlot);
    }

    @Unique
    public ItemStack damageItemStack(ItemStack stack, int amount) {
        if (!(stack instanceof CraftItemStack craftStack)) {
            return stack;
        }
        try {
            if (PAPERARC$CRAFT_STACK_GET_HANDLE_METHOD == null) {
                synchronized (CraftLivingEntityApiMixin.class) {
                    if (PAPERARC$CRAFT_STACK_GET_HANDLE_METHOD == null) {
                        PAPERARC$CRAFT_STACK_GET_HANDLE_METHOD = paperarc$method(CraftItemStack.class, "getHandle");
                    }
                }
            }
            net.minecraft.world.item.ItemStack nmsStack =
                (net.minecraft.world.item.ItemStack) PAPERARC$CRAFT_STACK_GET_HANDLE_METHOD.invoke(craftStack);
            nmsStack.hurtAndBreak(amount, this.getHandle(), net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            return craftStack;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: failed to damage item stack", e);
        }
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
        return (this.paperarc$shieldBlockingDelay != null ? this.paperarc$shieldBlockingDelay : (5));
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

    // region Shadows

    @Shadow
    public abstract RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode);
    // endregion

    // region helpers

    @Unique
    private static final Object PAPERARC_FIELD_LOCK = new Object();

    @Unique
    private static volatile Field PAPERARC_JUMPING_FIELD;

    @Unique
    private static volatile Field PAPERARC_USE_ITEM_REMAINING_FIELD;

    @Unique
    private static volatile Field PAPERARC_LAST_HURT_BY_PLAYER_FIELD;

    /** Resolves a declared field on NMS LivingEntity (mojmap name) once. */
    @Unique
    private static Field paperarc$nmsField(String name) {
        Field cached;
        switch (name) {
            case "jumping":
                cached = PAPERARC_JUMPING_FIELD;
                break;
            case "useItemRemaining":
                cached = PAPERARC_USE_ITEM_REMAINING_FIELD;
                break;
            case "lastHurtByPlayer":
                cached = PAPERARC_LAST_HURT_BY_PLAYER_FIELD;
                break;
            default:
                throw new IllegalArgumentException("unknown field " + name);
        }
        if (cached == null) {
            synchronized (PAPERARC_FIELD_LOCK) {
                switch (name) {
                    case "jumping":
                        cached = PAPERARC_JUMPING_FIELD;
                        break;
                    case "useItemRemaining":
                        cached = PAPERARC_USE_ITEM_REMAINING_FIELD;
                        break;
                    default:
                        cached = PAPERARC_LAST_HURT_BY_PLAYER_FIELD;
                        break;
                }
                if (cached == null) {
                    try {
                        cached = LivingEntity.class.getDeclaredField(name);
                        cached.setAccessible(true);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS LivingEntity." + name + " missing", e);
                    }
                    switch (name) {
                        case "jumping" -> PAPERARC_JUMPING_FIELD = cached;
                        case "useItemRemaining" -> PAPERARC_USE_ITEM_REMAINING_FIELD = cached;
                        default -> PAPERARC_LAST_HURT_BY_PLAYER_FIELD = cached;
                    }
                }
            }
        }
        return cached;
    }
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
        return ((com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge) this.getHandle()).paper$getUpwardsMovement();
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
     * 7. boolean isJumping()
     */
    @Unique
    public boolean isJumping() {
        try {
            return paperarc$nmsField("jumping").getBoolean(this.getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
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
        try {
            paperarc$nmsField("useItemRemaining").setInt(this.getHandle(), ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
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
        ((com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge) this.getHandle()).paper$setHurtDirection(direction);
    }

    /**
     * 18. void setJumping(boolean)
     */
    @Unique
    public void setJumping(boolean jumping) {
        try {
            paperarc$nmsField("jumping").setBoolean(this.getHandle(), jumping);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 19. void setKiller(Player)
     */
    @Unique
    public void setKiller(Player killer) {
        LivingEntity handle = this.getHandle();
        if (killer == null) {
            try {
                paperarc$nmsField("lastHurtByPlayer").set(handle, null);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            return;
        }
        net.minecraft.world.entity.player.Player nms =
                ((org.bukkit.craftbukkit.v.entity.CraftPlayer) killer).getHandle();
        try {
            paperarc$nmsField("lastHurtByPlayer").set(handle, nms);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
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
        ((com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge) this.getHandle()).paper$setShieldBlockingDelay(delay);
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
