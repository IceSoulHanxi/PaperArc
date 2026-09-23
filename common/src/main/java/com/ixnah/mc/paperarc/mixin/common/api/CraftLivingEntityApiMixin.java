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

    @Shadow
    public abstract LivingEntity getHandle();

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
        // vanilla 的 completeUsingItem() 是 protected，由 paperarc.accesswidener 放开
        this.getHandle().completeUsingItem();
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
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(craftStack);
        nmsStack.hurtAndBreak(amount, this.getHandle(), net.minecraft.world.entity.EquipmentSlot.MAINHAND);
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
        // Paper 把这个状态放在 NMS LivingEntity 上（LivingEntityFieldsMixin，默认 5）；
        // Craft 包装对象不保证唯一，读写都必须走 NMS 侧，否则 set 完再 get 拿不回来。
        return ((com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge) this.getHandle())
                .paper$getShieldBlockingDelay();
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
        // 原先写成 `maxDistance < 1.0E7 || …`，任何正常距离都会进这个分支 —— 整条射线检测
        // 其实从未成功执行过（B3-3 探针 P20 实测）。判据同 Bukkit：只要求 >= 1。
        if (maxDistance < 1) {
            throw new IllegalArgumentException("maxDistance must be positive");
        }
        Level level = handle.level();
        Vec3 from = handle.getEyePosition();
        Vec3 dir = Vec3.directionFromRotation(handle.getXRot(), handle.getYRot()).scale(maxDistance);
        Vec3 to = from.add(dir);
        AABB searchBox = handle.getBoundingBox().expandTowards(dir).inflate(1.0D);
        net.minecraft.world.phys.EntityHitResult nmsHit = ProjectileUtil.getEntityHitResult(level, handle, from, to,
                searchBox,
                (net.minecraft.world.entity.Entity e) -> !e.isSpectator() && e.isPickable() && e.isAlive(), 0.0f);
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
        // Paper：非玩家实体没有 hurtDir 存储，CraftLivingEntity 直接抛
        // （Expose-LivingEntity-hurt-direction.patch）；实现体在 CraftHumanEntity 上。
        throw new UnsupportedOperationException("Cannot set the hurt direction on a non player");
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
        net.minecraft.world.entity.player.Player nmsPlayer = killer == null
                ? null
                : ((org.bukkit.craftbukkit.v.entity.CraftPlayer) killer).getHandle();
        handle.setLastHurtByPlayer(nmsPlayer, nmsPlayer == null ? 0 : 100);
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

    // ===== B2-4：paper-api 缺口 =====

    @Unique
    public void heal(double amount, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason reason) {
        com.google.common.base.Preconditions.checkArgument(reason != null, "reason cannot be null");
        org.bukkit.entity.LivingEntity self = (org.bukkit.entity.LivingEntity) (Object) this;
        org.bukkit.event.entity.EntityRegainHealthEvent event =
                new org.bukkit.event.entity.EntityRegainHealthEvent(self, amount, reason);
        if (!event.callEvent()) {
            return;
        }
        self.setHealth(Math.min(self.getHealth() + event.getAmount(), self.getMaxHealth()));
    }

    @Unique
    public <T extends org.bukkit.entity.Projectile> T launchProjectile(
            Class<? extends T> projectile, org.bukkit.util.Vector velocity,
            java.util.function.Consumer<? super T> function) {
        // 偏差同 CraftBlockProjectileSourceApiMixin：Paper 在实体入世前跑 consumer，这里在之后。
        T launched = ((org.bukkit.projectiles.ProjectileSource) (Object) this)
                .launchProjectile(projectile, velocity);
        if (function != null && launched != null) {
            function.accept(launched);
        }
        return launched;
    }

    @Unique
    public void registerAttribute(org.bukkit.attribute.Attribute attribute) {
        com.google.common.base.Preconditions.checkArgument(attribute != null, "attribute cannot be null");
        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> holder =
                org.bukkit.craftbukkit.v.attribute.CraftAttribute.bukkitToMinecraftHolder(attribute);
        net.minecraft.world.entity.ai.attributes.AttributeMap map = this.getHandle().getAttributes();
        if (map.hasAttribute(holder)) {
            return;
        }
        // NMS AttributeMap 没有 registerAttribute（那是 Paper 加的），直接往
        // paperarc.accesswidener 放开的 attributes 表里塞一个默认实例。
        map.attributes.put(holder, new net.minecraft.world.entity.ai.attributes.AttributeInstance(
                holder, instance -> { }));
    }
}
