package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.Mob;
import org.bukkit.craftbukkit.v.entity.CraftMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * B20: paper-api org.bukkit.entity.Mob 新增抽象方法补齐（宿主 CraftMob）。
 * 实现参考 Paper 1.21.1 server 补丁中 CraftMob 的对应实现，NMS 调用翻译为 mojmap。
 */
@Mixin(CraftMob.class)
public abstract class CraftMobApiMixin {

    // Paper 用字段持有 Pathfinder（每个 CraftMob 一个）；注入 Craft 字段。
    @Unique
    private com.destroystokyo.paper.entity.Pathfinder pathfinder;

    @Shadow
    public abstract Mob getHandle();

    @Unique
    public int getHeadRotationSpeed() {
        return this.getHandle().getHeadRotSpeed();
    }
    public int getMaxHeadPitch() {
        return this.getHandle().getMaxHeadXRot();
    }

    @Unique
    public int getPossibleExperienceReward() {
        net.minecraft.world.entity.Mob handle = this.getHandle();
        return handle.getExperienceReward();
    }

    @Unique
    public boolean isAggressive() {
        return this.getHandle().isAggressive();
    }

    @Unique
    public boolean isInDaylight() {
        // Mob#isSunBurnTick 在 NMS 中为 protected，AT 加宽 m_21527_()Z 后直访
        return this.getHandle().isSunBurnTick();
    }

    @Unique
    public boolean isLeftHanded() {
        return this.getHandle().isLeftHanded();
    }

    @Unique
    public void lookAt(double x, double y, double z) {
        this.getHandle().getLookControl().setLookAt(x, y, z);
    }

    @Unique
    public void lookAt(double x, double y, double z, float headRotationSpeed, float maxHeadPitch) {
        this.getHandle().getLookControl().setLookAt(x, y, z, headRotationSpeed, maxHeadPitch);
    }

    @Unique
    public void lookAt(org.bukkit.Location location) {
        com.google.common.base.Preconditions.checkNotNull(location, "location cannot be null");
        com.google.common.base.Preconditions.checkArgument(
                location.getWorld().equals(((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getWorld()),
                "location in a different world");
        this.getHandle().getLookControl().setLookAt(location.getX(), location.getY(), location.getZ());
    }

    @Unique
    public void lookAt(org.bukkit.Location location, float headRotationSpeed, float maxHeadPitch) {
        com.google.common.base.Preconditions.checkNotNull(location, "location cannot be null");
        com.google.common.base.Preconditions.checkArgument(
                location.getWorld().equals(((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getWorld()),
                "location in a different world");
        this.getHandle().getLookControl().setLookAt(location.getX(), location.getY(), location.getZ(), headRotationSpeed, maxHeadPitch);
    }

    @Unique
    public void lookAt(org.bukkit.entity.Entity entity) {
        com.google.common.base.Preconditions.checkNotNull(entity, "entity cannot be null");
        com.google.common.base.Preconditions.checkArgument(
                entity.getWorld().equals(((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getWorld()),
                "entity in a different world");
        net.minecraft.world.entity.Entity nms = ((org.bukkit.craftbukkit.v.entity.CraftEntity) entity).getHandle();
        this.getHandle().getLookControl().setLookAt(nms);
    }

    @Unique
    public void lookAt(org.bukkit.entity.Entity entity, float headRotationSpeed, float maxHeadPitch) {
        com.google.common.base.Preconditions.checkNotNull(entity, "entity cannot be null");
        com.google.common.base.Preconditions.checkArgument(
                entity.getWorld().equals(((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getWorld()),
                "entity in a different world");
        net.minecraft.world.entity.Entity nms = ((org.bukkit.craftbukkit.v.entity.CraftEntity) entity).getHandle();
        this.getHandle().getLookControl().setLookAt(nms, headRotationSpeed, maxHeadPitch);
    }

    @Unique
    public void setAggressive(boolean aggressive) {
        this.getHandle().setAggressive(aggressive);
    }

    @Unique
    public void setLeftHanded(boolean leftHanded) {
        this.getHandle().setLeftHanded(leftHanded);
    }

    /**
     * Paper Mob Pathfinding API。每个 CraftMob 实例复用同一个 Pathfinder
     * （Paper 用字段持有；注入 Craft 字段），内部委托 NMS PathNavigation，
     * 见 com.ixnah.mc.paperarc.bridge.PaperPathfinder。
     */
    @Unique
    public com.destroystokyo.paper.entity.Pathfinder getPathfinder() {
        if (this.pathfinder == null) {
            this.pathfinder = new com.ixnah.mc.paperarc.bridge.PaperPathfinder(this.getHandle());
        }
        return this.pathfinder;
    }
    // ===== com.destroystokyo.paper.entity.RangedEntity（A4-3 父接口差集）=====
    // AbstractSkeleton/Drowned/Illusioner/Llama/Piglin/Pillager/Snowman/Witch/Wither
    // 共 9 个接口都 extends RangedEntity，对应的 NMS 实体都实现 RangedAttackMob，
    // 所以实现体只需挂在公共宿主 CraftMob 上一次。

    @Unique
    public void rangedAttack(org.bukkit.entity.LivingEntity target, float charge) {
        com.google.common.base.Preconditions.checkArgument(target != null, "target cannot be null");
        net.minecraft.world.entity.Mob handle = this.getHandle();
        com.google.common.base.Preconditions.checkState(
                handle instanceof net.minecraft.world.entity.monster.RangedAttackMob,
                "entity is not a ranged attacker");
        ((net.minecraft.world.entity.monster.RangedAttackMob) handle).performRangedAttack(
                ((org.bukkit.craftbukkit.v.entity.CraftLivingEntity) target).getHandle(), charge);
    }

    @Unique
    public void setChargingAttack(boolean raiseHands) {
        // 与 Paper 一致：抬手动作就是 vanilla 的 aggressive 标志
        this.getHandle().setAggressive(raiseHands);
    }

}
