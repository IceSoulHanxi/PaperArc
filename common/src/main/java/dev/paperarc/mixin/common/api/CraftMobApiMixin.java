package dev.paperarc.mixin.common.api;

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
        return handle.getExperienceReward((net.minecraft.server.level.ServerLevel) handle.level(), null);
    }

    @Unique
    public boolean isAggressive() {
        return this.getHandle().isAggressive();
    }

    @Unique
    private static java.lang.reflect.Method paperarc$sunBurnTickMethod;

    @Unique
    public boolean isInDaylight() {
        try {
            if (paperarc$sunBurnTickMethod == null) {
                // Mob#isSunBurnTick 在 NMS 中为 protected，跨包无法直接调用，反射读取（mojmap 运行时名一致）
                java.lang.reflect.Method m = Mob.class.getDeclaredMethod("isSunBurnTick");
                m.setAccessible(true);
                paperarc$sunBurnTickMethod = m;
            }
            return (Boolean) paperarc$sunBurnTickMethod.invoke(this.getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: reflect Mob#isSunBurnTick failed", e);
        }
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
     * （Paper 用字段持有；这里用 ApiState 弱键 side-map 缓存），内部委托 NMS
     * PathNavigation，见 dev.paperarc.bridge.PaperPathfinder。
     */
    @Unique
    public com.destroystokyo.paper.entity.Pathfinder getPathfinder() {
        com.destroystokyo.paper.entity.Pathfinder pathfinder =
                dev.paperarc.bridge.ApiState.get(this, "pathfinder", null);
        if (pathfinder == null) {
            pathfinder = new dev.paperarc.bridge.PaperPathfinder(this.getHandle());
            dev.paperarc.bridge.ApiState.put(this, "pathfinder", pathfinder);
        }
        return pathfinder;
    }
}
